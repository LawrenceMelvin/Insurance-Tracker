package com.project.lawrence.insurance_tracker.dto;

import java.time.LocalDate;
import java.util.Date;

public class InsuranceDTO {
    private int insuranceId;
    private String insuranceName;
    private String insuranceType;
    private int insurancePrice;
    private int insuranceCoverage;
    private LocalDate insuranceFromDate;
    private LocalDate insuranceToDate;


    // Getters and setters
    public int getInsuranceId() {
        return insuranceId;
    }

    public LocalDate getInsuranceFromDate() {
        return insuranceFromDate;
    }

    public void setInsuranceFromDate(LocalDate insuranceFromDate) {
        this.insuranceFromDate = insuranceFromDate;
    }

    public LocalDate getInsuranceToDate() {
        return insuranceToDate;
    }

    public void setInsuranceToDate(LocalDate insuranceToDate) {
        this.insuranceToDate = insuranceToDate;
    }

    public void setInsuranceId(int insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getInsuranceName() {
        return insuranceName;
    }

    public void setInsuranceName(String insuranceName) {
        this.insuranceName = insuranceName;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public int getInsurancePrice() {
        return insurancePrice;
    }

    public void setInsurancePrice(int insurancePrice) {
        this.insurancePrice = insurancePrice;
    }

    public int getInsuranceCoverage() {
        return insuranceCoverage;
    }

    public void setInsuranceCoverage(int insuranceCoverage) {
        this.insuranceCoverage = insuranceCoverage;
    }
}