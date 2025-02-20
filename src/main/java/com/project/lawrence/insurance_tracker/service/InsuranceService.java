package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class InsuranceService {
    @Autowired
    Insurancerepo repo;

    public List<Insurance> getInsuranceName() {
        System.out.println(repo.findAll());
        return repo.findAll();
    }

    public Insurance getInsuranceById(int id) {
        return repo.findById(id).orElse(null);
    }
}
