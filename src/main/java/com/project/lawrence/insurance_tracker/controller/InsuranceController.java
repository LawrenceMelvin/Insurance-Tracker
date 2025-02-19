package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/insurance")
public class InsuranceController {

    @Autowired
    InsuranceService service;

    @RequestMapping("/all")
    public List<Insurance> getInsuranceAllName(){
        return service.getInsuranceName();
    }

}
