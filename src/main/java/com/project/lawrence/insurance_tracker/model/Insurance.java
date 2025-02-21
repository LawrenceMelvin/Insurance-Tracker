package com.project.lawrence.insurance_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "insurance")
public class Insurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int insuranceId;
    @Column
    private String insuranceName;
    @Column
    private String insuranceType;
    @Column
    private int insurancePrice;
    @Column
    private int insuranceTerm;
    @Lob
    private byte[] insuranceDocument;

    public Insurance() {

    }

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

    public byte[] getInsuranceDocument() {
        return insuranceDocument;
    }

    public void setInsuranceDocument(byte[] insuranceDocument) {
        this.insuranceDocument = insuranceDocument;
    }

    @Override
    public String toString() {
        return "Insurance{" +
                "insuranceId=" + insuranceId +
                ", insuranceName='" + insuranceName + '\'' +
                ", insuranceType='" + insuranceType + '\'' +
                ", insurancePrice=" + insurancePrice +
                ", insuranceTerm=" + insuranceTerm +
                '}';
    }
}
