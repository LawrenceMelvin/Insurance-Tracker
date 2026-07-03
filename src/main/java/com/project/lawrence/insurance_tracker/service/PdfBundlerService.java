package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Claim;
import com.project.lawrence.insurance_tracker.model.ClaimDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfBundlerService {

    private final GcsStorageService storageService;

    public PdfBundlerService(GcsStorageService storageService) {
        this.storageService = storageService;
    }

    public byte[] bundleClaim(Claim claim, List<ClaimDocument> documents) throws IOException {
        try (PDDocument targetDoc = new PDDocument()) {
            
            // 1. Generate Cover Index Page
            generateCoverPage(targetDoc, claim, documents);

            // 2. Append each document
            for (ClaimDocument doc : documents) {
                try {
                    byte[] fileBytes = storageService.downloadFile(doc.getGcsFileUrl());
                    if (fileBytes == null || fileBytes.length == 0) continue;

                    if (isPdf(fileBytes)) {
                        try (PDDocument sourceDoc = PDDocument.load(fileBytes)) {
                            for (PDPage page : sourceDoc.getPages()) {
                                targetDoc.importPage(page);
                            }
                        }
                    } else {
                        // Image file (JPEG/PNG)
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
                            BufferedImage bim = ImageIO.read(bais);
                            if (bim != null) {
                                PDPage page = new PDPage(PDRectangle.A4);
                                targetDoc.addPage(page);
                                
                                PDImageXObject pdImage = LosslessFactory.createFromImage(targetDoc, bim);
                                try (PDPageContentStream contentStream = new PDPageContentStream(targetDoc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                                    // Scale image to fit A4 page preserving aspect ratio
                                    float pageW = PDRectangle.A4.getWidth();
                                    float pageH = PDRectangle.A4.getHeight();
                                    float imgW = pdImage.getWidth();
                                    float imgH = pdImage.getHeight();
                                    float scale = Math.min(pageW / imgW, pageH / imgH);
                                    
                                    float w = imgW * scale;
                                    float h = imgH * scale;
                                    float x = (pageW - w) / 2;
                                    float y = (pageH - h) / 2;
                                    
                                    contentStream.drawImage(pdImage, x, y, w, h);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error bundling document: " + doc.getFileName() + " -> " + e.getMessage());
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            targetDoc.save(baos);
            return baos.toByteArray();
        }
    }

    private void generateCoverPage(PDDocument document, Claim claim, List<ClaimDocument> documents) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
            // Draw title header
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 22);
            cs.newLineAtOffset(50, 750);
            cs.showText("Reimbursement Claim Summary Bundle");
            cs.endText();

            // Line separation
            cs.moveTo(50, 735);
            cs.lineTo(545, 735);
            cs.stroke();

            // Draw Meta Info
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, 700);
            cs.showText("Claim ID: ");
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.showText(String.valueOf(claim.getClaimId()));
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, 680);
            cs.showText("Insurer Name: ");
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.showText(claim.getInsurerName() != null ? claim.getInsurerName() : "N/A");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, 660);
            cs.showText("Diagnosis/Event: ");
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.showText(claim.getMedicalEvent() != null ? claim.getMedicalEvent() : "N/A");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, 640);
            cs.showText("Patient Name: ");
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.showText(claim.getBelongsToName() != null ? claim.getBelongsToName() : "Self");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, 620);
            cs.showText("Hospitalization Dates: ");
            cs.setFont(PDType1Font.HELVETICA, 12);
            String ad = claim.getAdmissionDate() != null ? claim.getAdmissionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
            String dd = claim.getDischargeDate() != null ? claim.getDischargeDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
            cs.showText(ad + " to " + dd);
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(50, 600);
            cs.showText("Total Expense Claimed: ");
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.showText(claim.getTotalClaimedAmount() != null ? claim.getTotalClaimedAmount().toString() : "0.0");
            cs.endText();

            // Table separation
            cs.moveTo(50, 570);
            cs.lineTo(545, 570);
            cs.stroke();

            // Document Index Table Title
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.newLineAtOffset(50, 550);
            cs.showText("Compiled Attachments Index");
            cs.endText();

            // Draw table headers
            int y = 520;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.newLineAtOffset(60, y);
            cs.showText("Category/Document Type");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.newLineAtOffset(250, y);
            cs.showText("File Name");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.newLineAtOffset(450, y);
            cs.showText("Amount");
            cs.endText();

            cs.moveTo(50, y - 5);
            cs.lineTo(545, y - 5);
            cs.stroke();

            // Draw rows
            cs.setFont(PDType1Font.HELVETICA, 10);
            for (ClaimDocument doc : documents) {
                y -= 20;
                if (y < 80) break; // Avoid page overflow on cover index

                cs.beginText();
                cs.newLineAtOffset(60, y);
                cs.showText(doc.getDocumentType() != null ? doc.getDocumentType() : "OTHER");
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(250, y);
                String fn = doc.getFileName();
                if (fn != null && fn.length() > 30) {
                    fn = fn.substring(0, 27) + "...";
                }
                cs.showText(fn != null ? fn : "N/A");
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(450, y);
                cs.showText(doc.getAmount() != null ? doc.getAmount().toString() : "-");
                cs.endText();
            }

            // Draw footer message
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
            cs.newLineAtOffset(50, 50);
            cs.showText("This bundle is auto-generated and indexed by InsureTracks AI Claim Assistant.");
            cs.endText();
        }
    }

    private boolean isPdf(byte[] data) {
        if (data == null || data.length < 4) return false;
        // Check PDF magic bytes "%PDF-"
        return data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46;
    }
}
