package com.project.lawrence.insurance_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
@Entity
@Table(name = "Insurance")
public class Insurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;
    @Column
    private String InsuranceName;
    @Column
    private String InsuranceType;
    @Column
    private int InsurancePrice;
    @Column
    private int InsuranceTerm;
}
