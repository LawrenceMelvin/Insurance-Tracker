package com.project.lawrence.insurance_tracker.dto;

public class InsuranceDTO {
    private int insuranceId;
    private String insuranceName;
    private String insuranceType;
    private int insurancePrice;
    private int insuranceTerm;

    // Getters and setters
    public int getInsuranceId() {
        return insuranceId;
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

    public int getInsuranceTerm() {
        return insuranceTerm;
    }

    public void setInsuranceTerm(int insuranceTerm) {
        this.insuranceTerm = insuranceTerm;
    }
}