package com.project.lawrence.insurance_tracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int claimId;

    @Column(nullable = false)
    private int insuranceId;

    @Column(nullable = false)
    private int userId;

    private String belongsToName;
    private String insurerName;
    private String medicalEvent;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private Double totalClaimedAmount;

    @Column(nullable = false)
    private String status; // "DRAFT", "SUBMITTED", "REIMBURSED"

    private String gcsBundleUrl;
    private LocalDateTime createdDate;

    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public int getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(int insuranceId) {
        this.insuranceId = insuranceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBelongsToName() {
        return belongsToName;
    }

    public void setBelongsToName(String belongsToName) {
        this.belongsToName = belongsToName;
    }

    public String getInsurerName() {
        return insurerName;
    }

    public void setInsurerName(String insurerName) {
        this.insurerName = insurerName;
    }

    public String getMedicalEvent() {
        return medicalEvent;
    }

    public void setMedicalEvent(String medicalEvent) {
        this.medicalEvent = medicalEvent;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public Double getTotalClaimedAmount() {
        return totalClaimedAmount;
    }

    public void setTotalClaimedAmount(Double totalClaimedAmount) {
        this.totalClaimedAmount = totalClaimedAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGcsBundleUrl() {
        return gcsBundleUrl;
    }

    public void setGcsBundleUrl(String gcsBundleUrl) {
        this.gcsBundleUrl = gcsBundleUrl;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
