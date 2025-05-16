package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@RequestMapping("/insurance")
public class InsuranceController {

    @Autowired
    InsuranceService service;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{insuranceId}")
    public ResponseEntity<Insurance> getById(@PathVariable int insuranceId){
        Insurance insurance = service.getInsuranceById(insuranceId);
        if (insurance != null) {
            return ResponseEntity.ok(insurance);  // Return 200 OK
        } else {
            return ResponseEntity.notFound().build();  // 404 Not Found
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addInsurance(@RequestBody Insurance request, Authentication authentication) {
        try {
            String username = authentication.getName();

            Insurance insurance = new Insurance();
            insurance.setInsuranceName(request.getInsuranceName());
            insurance.setInsuranceType(request.getInsuranceType());
            insurance.setInsurancePrice(request.getInsurancePrice());
            insurance.setInsuranceTerm(request.getInsuranceTerm());
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

    @GetMapping("/update/{insuranceId}")
    public String showUpdateForm(@PathVariable int insuranceId, Model model) {
        Insurance insurance = service.getInsuranceById(insuranceId);
        if (insurance != null) {
            model.addAttribute("insurance", insurance);
            return "updateInsurance";
        }
        return "redirect:/";
    }

    @PostMapping("/update/{insuranceId}")
    public String updateInsurance(@PathVariable int insuranceId, @ModelAttribute Insurance insurance, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUserName(username);
        Insurance existingInsurance = service.getInsuranceById(insuranceId);

        if (existingInsurance != null && existingInsurance.getUser().equals(user)) {
            insurance.setUser(user);
            insurance.setInsuranceId(insuranceId);
            service.updateInsurance(insurance);
        }
        return "redirect:/";
    }

    @DeleteMapping("/delete/{insuranceId}")
    public void deleteInsurance(@PathVariable int insuranceId){
        service.deleteInsurance(insuranceId);
    }

    @PatchMapping("/update/{insuranceId}")
    public ResponseEntity<Insurance> updateprice(@PathVariable int insuranceId, @PathVariable int insurancePrice){
        Insurance ins = service.updatePrice(insuranceId, insurancePrice);
        if (ins != null){
            return ResponseEntity.ok(ins);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

}
