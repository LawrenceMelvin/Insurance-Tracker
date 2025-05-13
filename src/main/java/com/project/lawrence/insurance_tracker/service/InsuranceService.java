package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class InsuranceService {

    @Autowired
    Insurancerepo repo;

    @Autowired
    UserRepository userRepository;

    public List<Insurance> getInsuranceByUser(User user) {
        return repo.findByUser(user);
    }

    public Insurance getInsuranceById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Insurance addInsurance(Insurance insurance, String userEmail){
        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        insurance.setUser(user);
        return repo.save(insurance);
    }

    public Insurance updateInsurance(Insurance insurance) {
        return repo.save(insurance);
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
}
