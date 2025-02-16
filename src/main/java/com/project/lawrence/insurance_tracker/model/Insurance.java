package com.project.lawrence.insurance_tracker.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class Insurance {
    private int Id;
    private String InsuranceName;
    private String InsuranceType;
    private int InsurancePrice;
    private int InsuranceTerm;
}
