package com.project.lawrence.insurance_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class PolicyExplainerService {

    private static final Logger logger = LoggerFactory.getLogger(PolicyExplainerService.class);
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * Main entry point to explain policy PDF.
     */
    public String explainPolicy(MultipartFile file) throws Exception {
        byte[] pdfBytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();

        // 1. Check Encryption / Password Protection
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            // PDF opened successfully without user password
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
            throw new IllegalArgumentException("The uploaded PDF is password-protected. Please upload an unlocked version.");
        } catch (IOException e) {
            logger.warn("PDF load warning during validation", e);
        }

        // 2. Detect if Vector PDF (has selectable text)
        boolean isVector = checkIsVectorPdf(pdfBytes);
        logger.info("PDF Ingestion Routing: isVector={}", isVector);

        byte[] processedBytes;
        if (isVector) {
            // Keep original vector PDF
            processedBytes = pdfBytes;
        } else {
            // Scanned PDF Path: Route and Rasterize
            processedBytes = handleScannedPdf(pdfBytes);
        }

        // 3. Stage to Google Files API
        String fileUri = null;
        String fileName = null; // e.g. "files/xxxx"
        try {
            Map<String, String> fileInfo = stageFileToGoogle(processedBytes, originalFilename != null ? originalFilename : "policy.pdf");
            fileUri = fileInfo.get("uri");
            fileName = fileInfo.get("name");
            logger.info("File successfully staged to Google Files API: uri={}, name={}", fileUri, fileName);

            // 4. Invoke Gemini Master Analysis
            return invokeGeminiMasterAnalysis(fileUri);

        } finally {
            // 5. Clean up from Google Files API immediately
            if (fileName != null) {
                purgeGoogleFile(fileName);
            }
        }
    }

    private boolean checkIsVectorPdf(byte[] pdfBytes) {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(Math.min(3, document.getNumberOfPages()));
            String text = stripper.getText(document);
            return text != null && text.trim().length() > 200;
        } catch (IOException e) {
            logger.error("Failed to read PDF pages to check vector status", e);
            return false;
        }
    }

    private byte[] handleScannedPdf(byte[] pdfBytes) throws Exception {
        int pageCount;
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            pageCount = doc.getNumberOfPages();
        }

        if (pageCount <= 15) {
            logger.info("Scanned PDF has {} pages (<= 15). Rasterizing all pages.", pageCount);
            List<Integer> pagesToRasterize = new ArrayList<>();
            for (int i = 1; i <= pageCount; i++) {
                pagesToRasterize.add(i);
            }
            return rasterizePagesAndStitch(pdfBytes, pagesToRasterize);
        }

        logger.info("Scanned PDF has {} pages (> 15). Running TOC router (Pass 1).", pageCount);
        // Pass 1: Rasterize pages 1-4 for TOC
        List<Integer> tocPages = new ArrayList<>();
        for (int i = 1; i <= Math.min(4, pageCount); i++) {
            tocPages.add(i);
        }
        byte[] tocPdfBytes = rasterizePagesAndStitch(pdfBytes, tocPages);

        // Upload TOC PDF to Files API
        Map<String, String> tocFileInfo = stageFileToGoogle(tocPdfBytes, "toc.pdf");
        String tocFileUri = tocFileInfo.get("uri");
        String tocFileName = tocFileInfo.get("name");

        List<Integer> targetPages = new ArrayList<>();
        try {
            // Call Gemini to route target pages from TOC
            String tocJson = invokeGeminiTocRouter(tocFileUri);
            logger.info("TOC Router Response: {}", tocJson);
            targetPages = parsePageRangesFromToc(tocJson, pageCount);
        } catch (Exception e) {
            logger.error("Failed to route pages via TOC router. Falling back to first 15 pages.", e);
        } finally {
            if (tocFileName != null) {
                purgeGoogleFile(tocFileName);
            }
        }

        if (targetPages.isEmpty()) {
            // Fallback: Rasterize first 15 pages
            logger.warn("No pages routed or TOC mapping failed. Rasterizing first 15 pages.");
            for (int i = 1; i <= Math.min(15, pageCount); i++) {
                targetPages.add(i);
            }
        }

        logger.info("TOC Router matched pages to analyze: {}", targetPages);
        return rasterizePagesAndStitch(pdfBytes, targetPages);
    }

    private byte[] rasterizePagesAndStitch(byte[] pdfBytes, List<Integer> pageNumbers) throws IOException {
        try (PDDocument sourceDoc = PDDocument.load(pdfBytes)) {
            try (PDDocument targetDoc = new PDDocument()) {
                PDFRenderer renderer = new PDFRenderer(sourceDoc);
                for (int pageNum : pageNumbers) {
                    int zeroIdx = pageNum - 1;
                    if (zeroIdx >= 0 && zeroIdx < sourceDoc.getNumberOfPages()) {
                        BufferedImage image = renderer.renderImageWithDPI(zeroIdx, 150); // 150 DPI is standard and fast
                        PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                        targetDoc.addPage(page);

                        PDImageXObject pdImage = LosslessFactory.createFromImage(targetDoc, image);
                        try (PDPageContentStream contentStream = new PDPageContentStream(targetDoc, page)) {
                            contentStream.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
                        }
                    }
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                targetDoc.save(baos);
                return baos.toByteArray();
            }
        }
    }

    private Map<String, String> stageFileToGoogle(byte[] fileBytes, String filename) throws Exception {
        // Step 1: Initialize Resumable Upload
        String initUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + apiKey;
        String initBody = objectMapper.writeValueAsString(Map.of(
                "file", Map.of("displayName", filename)
        ));

        HttpRequest initRequest = HttpRequest.newBuilder()
                .uri(URI.create(initUrl))
                .header("X-Goog-Upload-Protocol", "resumable")
                .header("X-Goog-Upload-Command", "start")
                .header("X-Goog-Upload-Header-Content-Length", String.valueOf(fileBytes.length))
                .header("X-Goog-Upload-Header-Content-Type", "application/pdf")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(initBody))
                .build();

        HttpResponse<String> initResponse = httpClient.send(initRequest, HttpResponse.BodyHandlers.ofString());
        if (initResponse.statusCode() != 200) {
            throw new RuntimeException("Failed to initialize Google Files API upload. Status: " + initResponse.statusCode() + ", Body: " + initResponse.body());
        }

        String uploadUrl = initResponse.headers().firstValue("X-Goog-Upload-URL")
                .orElseThrow(() -> new RuntimeException("Missing X-Goog-Upload-URL header in Google Files API initialization response."));

        // Step 2: Upload Files Bytes
        HttpRequest uploadRequest = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("X-Goog-Upload-Command", "upload, finalize")
                .header("X-Goog-Upload-Offset", "0")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        HttpResponse<String> uploadResponse = httpClient.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        if (uploadResponse.statusCode() != 200) {
            throw new RuntimeException("Failed to upload file bytes to Google Files API. Status: " + uploadResponse.statusCode() + ", Body: " + uploadResponse.body());
        }

        JsonNode responseJson = objectMapper.readTree(uploadResponse.body());
        JsonNode fileNode = responseJson.get("file");
        if (fileNode == null) {
            throw new RuntimeException("Invalid response format from Google Files API upload: " + uploadResponse.body());
        }

        Map<String, String> result = new HashMap<>();
        result.put("name", fileNode.get("name").asText());
        result.put("uri", fileNode.get("uri").asText());
        return result;
    }

    private void purgeGoogleFile(String fileName) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/" + fileName + "?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Successfully deleted temporary staged file: {}", fileName);
            } else {
                logger.warn("Failed to delete temporary staged file: {}, Status: {}, Body: {}", fileName, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            logger.error("Error occurred while deleting temporary staged file: " + fileName, e);
        }
    }

    private String invokeGeminiTocRouter(String fileUri) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String prompt = "You are an insurance document router. Analyze the attached pages (which represent the Table of Contents of a multi-page insurance contract) and determine the exact 1-indexed page ranges or specific page numbers that contain the following sections. "
                + "Return a JSON block matching the schema:\n"
                + "{\n"
                + "  \"scheduleOfBenefitsPages\": [5, 6, 7],\n"
                + "  \"exclusionsPages\": [12, 13, 14],\n"
                + "  \"waitingPeriodsPages\": [20, 21],\n"
                + "  \"claimsProcedurePages\": [35, 36],\n"
                + "  \"generalInfoPages\": [1, 2]\n"
                + "}\n"
                + "If a section is not found in the TOC, return an empty array for that section. Be extremely accurate.";

        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("fileData", Map.of("fileUri", fileUri, "mimeType", "application/pdf")),
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini TOC routing request failed. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode responseNode = objectMapper.readTree(response.body());
        return responseNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
    }

    private List<Integer> parsePageRangesFromToc(String tocJson, int totalPages) {
        List<Integer> pages = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(tocJson);
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                JsonNode arrayNode = root.get(field);
                if (arrayNode != null && arrayNode.isArray()) {
                    for (JsonNode val : arrayNode) {
                        int p = val.asInt();
                        if (p >= 1 && p <= totalPages && !pages.contains(p)) {
                            pages.add(p);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse TOC page mappings JSON", e);
        }
        Collections.sort(pages);
        return pages;
    }

    private String invokeGeminiMasterAnalysis(String fileUri) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String prompt = "Analyze the attached insurance policy document. Extract the details according to the following JSON structure. If any details are not present in the document, set them to null or empty arrays.\n"
                + "Ensure that dates are in yyyy-MM-dd format.\n"
                + "The insuranceType must be one of: health, life, home, auto, travel, disability.\n"
                + "Return only a valid JSON object matching this structure:\n"
                + "{\n"
                + "  \"policyNumber\": String (the policy number or identifier),\n"
                + "  \"insurerName\": String (e.g. Blue Cross, Aetna, Geico, Allianz),\n"
                + "  \"policyholderName\": String (primary insured or policy owner),\n"
                + "  \"effectiveDate\": String (yyyy-MM-dd),\n"
                + "  \"expirationDate\": String (yyyy-MM-dd),\n"
                + "  \"insuranceType\": String (health/life/home/auto/travel/disability),\n"
                + "  \"healthDetails\": {\n"
                + "    \"deductiblesIndividual\": Number,\n"
                + "    \"deductiblesFamily\": Number,\n"
                + "    \"coinsurancePercentage\": Number (e.g. 20.0 for 20%),\n"
                + "    \"outOfPocketMaximum\": Number,\n"
                + "    \"preExistingConditionsLimitations\": String,\n"
                + "    \"copaymentsSpecialist\": Number\n"
                + "  },\n"
                + "  \"lifeDetails\": {\n"
                + "    \"deathBenefitAmount\": Number,\n"
                + "    \"beneficiaryRevocability\": String (Revocable / Irrevocable),\n"
                + "    \"suicideExclusionPeriodMonths\": Integer,\n"
                + "    \"contestabilityPeriodYears\": Integer,\n"
                + "    \"accidentalDeathRiderPayout\": Number\n"
                + "  },\n"
                + "  \"homeDetails\": {\n"
                + "    \"dwellingLimit\": Number,\n"
                + "    \"otherStructuresLimit\": Number,\n"
                + "    \"personalPropertyLimit\": Number,\n"
                + "    \"occupancyRestrictionClause\": String,\n"
                + "    \"hazardExclusions\": Array of String,\n"
                + "    \"deductibleWindHail\": Number\n"
                + "  },\n"
                + "  \"autoDetails\": {\n"
                + "    \"deductibleIndividual\": Number,\n"
                + "    \"deductibleCompulsory\": Number,\n"
                + "    \"deductibleVoluntary\": Number,\n"
                + "    \"zeroDepreciationCoverage\": String (details if zero-dep/bumper-to-bumper is included),\n"
                + "    \"noClaimBonusPercentage\": Number,\n"
                + "    \"insuredDeclaredValueIdv\": Number,\n"
                + "    \"exclusions\": Array of String\n"
                + "  },\n"
                + "  \"disabilityDetails\": {\n"
                + "    \"eliminationPeriodDays\": Integer,\n"
                + "    \"definitionOfDisability\": String (e.g. Own Occupation vs Any Occupation),\n"
                + "    \"monthlyBenefitPercentage\": Number,\n"
                + "    \"benefitDurationPeriod\": String,\n"
                + "    \"mentalHealthLimitationMonths\": Integer\n"
                + "  },\n"
                + "  \"unusualExclusionsRedFlags\": Array of String (clauses that might catch the user off-guard, such as exclusion of certain treatments, co-pay clauses on specific treatments, room rent caps, age limits, waiting periods, adventure sports, cancellation penalties, renewal subcharges, etc.),\n"
                + "  \"claimsSubmissionProcedure\": String (step-by-step instructions for submitting a claim or reimbursement)\n"
                + "}";

        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("fileData", Map.of("fileUri", fileUri, "mimeType", "application/pdf")),
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "thinkingConfig", Map.of("thinkingBudget", 2048)
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini master analysis request failed. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode responseNode = objectMapper.readTree(response.body());
        return responseNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
    }
}
