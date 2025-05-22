package com.project.lawrence.insurance_tracker.model;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
@Entity
@Table(name = "insurance")
public class Insurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int insuranceId;
    @Column(nullable = false)
    private String insuranceName;
    @Column(nullable = false)
    private String insuranceType;
    @Column(nullable = false)
    private int insurancePrice;
    @Column(nullable = false)
    private LocalDate insuranceFromDate;
    @Column(nullable = false)
    private LocalDate insuranceToDate;
//    private byte[] insuranceDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public Insurance() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    @Override
    public String toString() {
        return "Insurance{" +
                "insuranceId=" + insuranceId +
                ", insuranceName='" + insuranceName + '\'' +
                ", insuranceType='" + insuranceType + '\'' +
                ", insurancePrice=" + insurancePrice +
                '}';
    }
}
