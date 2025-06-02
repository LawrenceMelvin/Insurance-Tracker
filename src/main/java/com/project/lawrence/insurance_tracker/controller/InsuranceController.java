package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@RequestMapping("/insurance")
public class InsuranceController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    InsuranceService service;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{insuranceId}")
    public ResponseEntity<InsuranceDTO> getById(@PathVariable int insuranceId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthorized access attempt to insurance ID: {}", insuranceId);
            return ResponseEntity.status(401).build();
        }
        InsuranceDTO insuranceDTO = service.mapToDTO(service.getInsuranceById(insuranceId));
        return ResponseEntity.ok(insuranceDTO);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addInsurance(@RequestBody Insurance request, Authentication authentication) {
        try {
            String username = authentication.getName();

            Insurance insurance = new Insurance();
            insurance.setInsuranceName(request.getInsuranceName());
            insurance.setInsuranceType(request.getInsuranceType());
            insurance.setInsurancePrice(request.getInsurancePrice());
            insurance.setInsuranceCoverage(request.getInsuranceCoverage());
            insurance.setInsuranceFromDate(request.getInsuranceFromDate());
            insurance.setInsuranceToDate(request.getInsuranceToDate());
            Insurance savedInsurance = service.addInsurance(insurance, username);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Insurance added successfully",
                    "insuranceId", savedInsurance.getInsuranceId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
            ));
        }
    }

    @PutMapping("/update/{insuranceId}")
    public ResponseEntity<?> updateInsurance(@PathVariable int insuranceId, @RequestBody InsuranceDTO insuranceDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }

        String username = principal.getName();
        User user = userRepository.findByUserEmail(username).orElse(null);
        logger.info("User: {}", user);
        Insurance existingInsurance = service.getInsuranceById(insuranceId);
        logger.info("Existing User: {}", existingInsurance.getUser().getUserEmail());
        if (existingInsurance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Insurance not found");
        }

        if (!existingInsurance.getUser().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this insurance");
        }

        existingInsurance.setInsuranceName(insuranceDTO.getInsuranceName());
        existingInsurance.setInsuranceType(insuranceDTO.getInsuranceType());
        existingInsurance.setInsurancePrice(insuranceDTO.getInsurancePrice());
        existingInsurance.setInsuranceCoverage(insuranceDTO.getInsuranceCoverage());
        existingInsurance.setInsuranceFromDate(insuranceDTO.getInsuranceFromDate());
        existingInsurance.setInsuranceToDate(insuranceDTO.getInsuranceToDate());
        service.updateInsurance(existingInsurance);

        return ResponseEntity.ok("Insurance updated successfully");
    }

    @DeleteMapping("/delete/{insuranceId}")
    public ResponseEntity<?> deleteInsurance(@PathVariable int insuranceId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }

        try {
            service.deleteInsurance(insuranceId);
            return ResponseEntity.ok("Insurance deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete insurance");
        }
    }


}
