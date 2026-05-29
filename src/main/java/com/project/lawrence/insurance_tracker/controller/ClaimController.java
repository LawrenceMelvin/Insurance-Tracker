package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Claim;
import com.project.lawrence.insurance_tracker.model.ClaimDocument;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.ClaimDocumentRepository;
import com.project.lawrence.insurance_tracker.repository.ClaimRepository;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.ClaimAssistantService;
import com.project.lawrence.insurance_tracker.service.GcsStorageService;
import com.project.lawrence.insurance_tracker.service.PdfBundlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/claims")
public class ClaimController {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimDocumentRepository claimDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GcsStorageService storageService;

    @Autowired
    private ClaimAssistantService claimAssistantService;

    @Autowired
    private PdfBundlerService pdfBundlerService;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Unauthorized");
        }
        return userRepository.findByUserEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getClaims(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(claimRepository.findByUserId(user.getUserId()));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<Map<String, Object>> getClaimDetails(@PathVariable int claimId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        if (claim.getUserId() != user.getUserId()) {
            return ResponseEntity.status(403).build();
        }

        List<ClaimDocument> docs = claimDocumentRepository.findByClaimId(claimId);

        Map<String, Object> response = new HashMap<>();
        response.put("claim", claim);
        response.put("documents", docs);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checklist")
    public ResponseEntity<?> getChecklist(@RequestParam String insurerName, @RequestParam String medicalEvent) {
        ClaimAssistantService.ClaimChecklistRecord checklist = claimAssistantService.generateChecklist(insurerName, medicalEvent);
        return ResponseEntity.ok(checklist);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClaim(@RequestBody Map<String, Object> payload, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        int insuranceId = Integer.parseInt(payload.get("insuranceId").toString());
        String belongsToName = (String) payload.get("belongsToName");
        String insurerName = (String) payload.get("insurerName");
        String medicalEvent = (String) payload.get("medicalEvent");
        Double totalClaimedAmount = Double.parseDouble(payload.get("totalClaimedAmount").toString());
        
        LocalDate admissionDate = LocalDate.parse(payload.get("admissionDate").toString());
        LocalDate dischargeDate = LocalDate.parse(payload.get("dischargeDate").toString());

        Claim claim;
        if (payload.containsKey("claimId") && payload.get("claimId") != null && !payload.get("claimId").toString().isEmpty()) {
            int existingClaimId = Integer.parseInt(payload.get("claimId").toString());
            claim = claimRepository.findById(existingClaimId)
                    .orElse(new Claim());
            if (claim.getUserId() != 0 && claim.getUserId() != user.getUserId()) {
                return ResponseEntity.status(403).build();
            }
        } else {
            claim = new Claim();
            claim.setUserId(user.getUserId());
            claim.setCreatedDate(LocalDateTime.now());
        }

        claim.setInsuranceId(insuranceId);
        claim.setBelongsToName(belongsToName);
        claim.setInsurerName(insurerName);
        claim.setMedicalEvent(medicalEvent);
        claim.setTotalClaimedAmount(totalClaimedAmount);
        claim.setAdmissionDate(admissionDate);
        claim.setDischargeDate(dischargeDate);
        claim.setStatus("DRAFT");

        Claim savedClaim = claimRepository.save(claim);

        ClaimAssistantService.ClaimChecklistRecord checklist = claimAssistantService.generateChecklist(insurerName, medicalEvent);

        Map<String, Object> response = new HashMap<>();
        response.put("claim", savedClaim);
        response.put("checklist", checklist);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimId}/upload")
    public ResponseEntity<?> uploadDocument(
            @PathVariable int claimId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        if (claim.getUserId() != user.getUserId()) {
            return ResponseEntity.status(403).build();
        }

        try {
            String fileUrl = storageService.uploadFile(file, "claims/" + claimId);

            ClaimDocument doc = new ClaimDocument();
            doc.setClaimId(claimId);
            doc.setFileName(file.getOriginalFilename());
            doc.setGcsFileUrl(fileUrl);
            doc.setDocumentType("OTHER");
            doc.setMatched(false);

            ClaimDocument savedDoc = claimDocumentRepository.save(doc);

            ClaimAssistantService.DocumentClassificationRecord aiResult = claimAssistantService.classifyDocument(savedDoc);
            
            savedDoc.setDocumentType(aiResult.documentType());
            if (aiResult.billAmount() != null) {
                savedDoc.setAmount(aiResult.billAmount());
            }
            ClaimDocument finalSavedDoc = claimDocumentRepository.save(savedDoc);

            return ResponseEntity.ok(finalSavedDoc);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload document: " + e.getMessage());
        }
    }

    @PostMapping("/{claimId}/audit")
    public ResponseEntity<?> auditClaim(
            @PathVariable int claimId,
            @RequestBody List<String> checklistRequirements,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        if (claim.getUserId() != user.getUserId()) {
            return ResponseEntity.status(403).build();
        }

        List<ClaimDocument> docs = claimDocumentRepository.findByClaimId(claimId);

        ClaimAssistantService.PreAuditReportRecord auditResult = claimAssistantService.auditClaim(claim, docs, checklistRequirements);

        return ResponseEntity.ok(auditResult);
    }

    @PostMapping("/{claimId}/bundle")
    public ResponseEntity<?> bundleClaim(
            @PathVariable int claimId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        if (claim.getUserId() != user.getUserId()) {
            return ResponseEntity.status(403).build();
        }

        List<ClaimDocument> docs = claimDocumentRepository.findByClaimId(claimId);

        try {
            byte[] pdfBytes = pdfBundlerService.bundleClaim(claim, docs);

            MultipartFile mockFile = new MultipartFile() {
                @Override
                public String getName() { return "claim_bundle_" + claimId + ".pdf"; }
                @Override
                public String getOriginalFilename() { return "claim_bundle_" + claimId + ".pdf"; }
                @Override
                public String getContentType() { return "application/pdf"; }
                @Override
                public boolean isEmpty() { return false; }
                @Override
                public long getSize() { return pdfBytes.length; }
                @Override
                public byte[] getBytes() { return pdfBytes; }
                @Override
                public java.io.InputStream getInputStream() { return new ByteArrayInputStream(pdfBytes); }
                @Override
                public void transferTo(java.io.File dest) throws java.io.IOException, IllegalStateException {
                    throw new UnsupportedOperationException();
                }
            };

            String bundleUrl = storageService.uploadFile(mockFile, "claims/" + claimId + "/bundles");

            claim.setGcsBundleUrl(bundleUrl);
            claim.setStatus("SUBMITTED");
            claimRepository.save(claim);

            return ResponseEntity.ok(Map.of("bundleUrl", bundleUrl));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to compile claims bundle: " + e.getMessage());
        }
    }

    @DeleteMapping("/{claimId}")
    public ResponseEntity<?> deleteClaim(@PathVariable int claimId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        if (claim.getUserId() != user.getUserId()) {
            return ResponseEntity.status(403).build();
        }

        // Delete associated files
        List<ClaimDocument> docs = claimDocumentRepository.findByClaimId(claimId);
        for (ClaimDocument doc : docs) {
            storageService.deleteFile(doc.getGcsFileUrl());
            claimDocumentRepository.delete(doc);
        }

        // Delete main claim bundle if exists
        if (claim.getGcsBundleUrl() != null) {
            storageService.deleteFile(claim.getGcsBundleUrl());
        }

        claimRepository.delete(claim);
        return ResponseEntity.ok().build();
    }
}
