package com.project.lawrence.insurance_tracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "claim_documents")
public class ClaimDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int documentId;

    @Column(nullable = false)
    private int claimId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1024)
    private String gcsFileUrl;

    private String documentType; // "DISCHARGE_SUMMARY", "FINAL_BILL", "PHARMACY_BILL", "LAB_REPORT", "KYC", "OTHER"
    private Double amount;
    private boolean isMatched;

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getGcsFileUrl() {
        return gcsFileUrl;
    }

    public void setGcsFileUrl(String gcsFileUrl) {
        this.gcsFileUrl = gcsFileUrl;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }
}
