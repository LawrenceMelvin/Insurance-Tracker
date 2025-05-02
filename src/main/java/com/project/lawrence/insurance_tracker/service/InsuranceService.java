package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class InsuranceService {

    @Autowired
    Insurancerepo repo;

    public List<Insurance> getInsuranceName() {
        System.out.println(repo.findAll());
        return repo.findAll();
    }

    public List<Insurance> getInsuranceByUser(User user) {
        return repo.findByUser(user);
    }

    public Insurance getInsuranceById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Insurance addInsurance(Insurance insurance){
        return repo.save(insurance);
    }

    public Insurance updateInsurance(int id,Insurance updatedInsurance){
        return repo.findById(id).map(insurance -> {
            insurance.setInsuranceName(updatedInsurance.getInsuranceName());
            insurance.setInsuranceType(updatedInsurance.getInsuranceType());
            insurance.setInsurancePrice(updatedInsurance.getInsurancePrice());
            insurance.setInsuranceTerm(updatedInsurance.getInsuranceTerm());
            return repo.save(insurance);
        }).orElse(null);
    }

    public void deleteInsurance(int insuranceId){
        if (repo.existsById(insuranceId)) {
            repo.deleteById(insuranceId);
        }
    }

    public Insurance updatePrice(int insuranceId, int insurancePrice){
        return repo.findById(insuranceId).map(insurance -> {
            insurance.setInsurancePrice(insurancePrice);
            return repo.save(insurance);
        }).orElse(null);
    }

    public List<Insurance> searchByName(String name) {
        return repo.findByInsuranceNameContainingIgnoreCase(name);
    }

    public List<Insurance> searchByType(String type) {
        return repo.findByInsuranceTypeIgnoreCase(type);
    }

    public void uploadInsuranceDocument(int insuranceId, MultipartFile file) throws IOException {
        Insurance insurance = repo.findById(insuranceId).orElseThrow(() -> new RuntimeException("Not found"));
        insurance.setInsuranceDocument(file.getBytes());
        repo.save(insurance);
    }
}
