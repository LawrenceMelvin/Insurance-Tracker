package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
public class InsuranceService {
    @Autowired
    Insurance ins;



    public String getAllInsurance(){
        return ins.getInsuranceName();
    }

}
