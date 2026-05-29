package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Claim;
import com.project.lawrence.insurance_tracker.model.ClaimDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(ClaimAssistantService.class);
    private final ChatClient chatClient;
    private final GcsStorageService storageService;

    public ClaimAssistantService(ChatClient.Builder builder, GcsStorageService storageService) {
        this.chatClient = builder.build();
        this.storageService = storageService;
    }

    public record ClaimChecklistRecord(
        List<String> requiredDocuments,
        List<String> tips
    ) {}

    public record DocumentClassificationRecord(
        String documentType, // "DISCHARGE_SUMMARY", "FINAL_BILL", "PHARMACY_BILL", "LAB_REPORT", "KYC", "OTHER"
        Double billAmount,
        String documentDate,
        String patientName
    ) {}

    public record PreAuditReportRecord(
        boolean isValid,
        List<String> warnings,
        List<String> strengths
    ) {}

    // 1. Generate Checklist based on Insurer and Diagnosis/Event
    public ClaimChecklistRecord generateChecklist(String insurer, String medicalEvent) {
        String prompt = String.format("""
            You are a professional medical insurance claims manager.
            The user wants to file a reimbursement claim for the insurer: %s.
            The medical event or diagnosis is: %s.
            
            Generate a detailed, step-by-step document checklist that the user needs to compile to ensure successful reimbursement.
            Provide:
            1. requiredDocuments: List of specific document types required (e.g. "Hospital Discharge Summary", "Itemized Pharmacy Bill").
            2. tips: Insurer-specific tips to avoid rejection (e.g. "Star Health requires original stamps on the final bill").
            
            Format the output strictly as JSON matching the requested structure.
            """, insurer, medicalEvent);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(ClaimChecklistRecord.class);
        } catch (Exception e) {
            logger.error("Error generating claim checklist via Gemini", e);
            // Default generic checklist
            return new ClaimChecklistRecord(
                List.of("Discharge Summary", "Final Consolidated Bill", "Prescriptions", "KYC Document"),
                List.of("Ensure all bills are stamped by the hospital administration.")
            );
        }
    }

    // 2. Classify and Extract data from uploaded document bytes
    public DocumentClassificationRecord classifyDocument(ClaimDocument doc) {
        try {
            byte[] fileBytes = storageService.downloadFile(doc.getGcsFileUrl());
            if (fileBytes == null || fileBytes.length == 0) {
                return new DocumentClassificationRecord("OTHER", null, null, null);
            }

            MimeType mimeType = getMimeType(doc.getFileName());
            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            String prompt = """
                Analyze the attached document (image/pdf) and classify it.
                Extract:
                1. documentType: Exactly one of "DISCHARGE_SUMMARY", "FINAL_BILL", "PHARMACY_BILL", "LAB_REPORT", "KYC", or "OTHER".
                2. billAmount: If this document has a total bill amount, extract it as a double value. Otherwise, return null.
                3. documentDate: Extract any date associated with the document in YYYY-MM-DD format.
                4. patientName: Extract the patient's name listed on the document.
                """;

            return chatClient.prompt()
                    .user(promptSpec -> promptSpec
                        .text(prompt)
                        .media(mimeType, resource)
                    )
                    .call()
                    .entity(DocumentClassificationRecord.class);

        } catch (Exception e) {
            logger.error("Error classifying document " + doc.getFileName() + " via Gemini", e);
            return new DocumentClassificationRecord("OTHER", null, null, null);
        }
    }

    // 3. Pre-Audit verification engine
    public PreAuditReportRecord auditClaim(Claim claim, List<ClaimDocument> docs, List<String> checklist) {
        String docsText = docs.stream().map(
                d -> String.format(
                        "File: %s, Category: %s, Amount: %s, Patient: %s",
                        d.getFileName(),
                        d.getDocumentType(),
                        d.getAmount() != null ? d.getAmount() : "N/A",
                        d.getFileName() // Use file name as proxy
                )
        ).collect(Collectors.joining("\n"));

        String checklistText = String.join("\n- ", checklist);

        String prompt = String.format("""
            You are a professional medical insurance auditor. Run a Pre-Audit scan on the user's uploaded documents.
            
            Compare the uploaded files against the required checklist:
            Required Checklist:
            - %s
            
            Uploaded Files:
            %s
            
            Insurer Name: %s
            Medical Event: %s
            Total Claimed: %s
            
            Evaluate and return a structured JSON report with:
            1. isValid: True if all critical documents (Discharge Summary, Final Bill) are present and match. False if critical items are missing or there are math/prescription discrepancies.
            2. warnings: Detailed warnings about missing items, name mismatches, or missing prescriptions for medicine bills.
            3. strengths: Highlight positive aspects (e.g. "All KYC documents are verified", "Final bill has a matching discharge summary").
            
            Common audit rules to run:
            - Is there a 'PHARMACY_BILL' but no matching doctor's prescription? (Flag as warning).
            - Do the dates on bills match the hospitalization admission/discharge dates?
            - Does the patient name on documents match the claim profile?
            - Are key documents (Discharge Summary or Final Bill) missing?
            """, checklistText, docsText, claim.getInsurerName(), claim.getMedicalEvent(), claim.getTotalClaimedAmount());

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(PreAuditReportRecord.class);
        } catch (Exception e) {
            logger.error("Error auditing claim via Gemini", e);
            return new PreAuditReportRecord(
                false,
                List.of("Failed to perform AI audit. Please verify documents manually."),
                List.of()
            );
        }
    }

    private MimeType getMimeType(String fileName) {
        if (fileName == null) return MimeTypeUtils.APPLICATION_OCTET_STREAM;
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return MimeType.valueOf("application/pdf");
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MimeTypeUtils.IMAGE_JPEG;
        if (lower.endsWith(".png")) return MimeTypeUtils.IMAGE_PNG;
        return MimeTypeUtils.APPLICATION_OCTET_STREAM;
    }
}
