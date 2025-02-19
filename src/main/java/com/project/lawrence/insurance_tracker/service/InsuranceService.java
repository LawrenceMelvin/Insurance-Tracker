package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsuranceService {
    @Autowired
    Insurancerepo repo;

    public List<Insurance> getInsuranceName() {
        return repo.findAll();
    }

}
