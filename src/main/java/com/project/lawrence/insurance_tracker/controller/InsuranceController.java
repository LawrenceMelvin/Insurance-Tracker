package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
@RequestMapping("/insurance")
public class InsuranceController {

    @Autowired
    InsuranceService service;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/all")
    public ResponseEntity<List<Insurance>> getInsuranceAllName(){
        return new ResponseEntity<>(service.getInsuranceName(), HttpStatus.OK);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Insurance>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(service.searchByName(name));
    }

    // ✅ Search by Type
    @GetMapping("/search/type")
    public ResponseEntity<List<Insurance>> searchByType(@RequestParam String type) {
        return ResponseEntity.ok(service.searchByType(type));
    }

    @GetMapping("/{insuranceId}")
    public ResponseEntity<Insurance> getById(@PathVariable int insuranceId){
        Insurance insurance = service.getInsuranceById(insuranceId);
        if (insurance != null) {
            return ResponseEntity.ok(insurance);  // Return 200 OK
        } else {
            return ResponseEntity.notFound().build();  // 404 Not Found
        }
    }

    @GetMapping("/add")
    public String ShowaddInsurancePage() {
        return "addInsurance"; // Load addInsurance.html
    }

    @PostMapping("/add")
    public String addInsurance(@ModelAttribute Insurance insurance, HttpSession session){
        String username = (String) session.getAttribute("user");
        User user = userRepository.findByUserName(username);
        insurance.setUser(user);
        service.addInsurance(insurance);
        return "redirect:/"; // Redirect to the list of insurances
    }

    @PutMapping("/update/{insuranceId}")
    public ResponseEntity<Insurance> updateInsurance(@PathVariable int insuranceId,@RequestBody Insurance updatedInsurance){
        Insurance insurance = service.updateInsurance(insuranceId, updatedInsurance);
        if (insurance != null) {
            return ResponseEntity.ok(insurance);  // Return updated record
        } else {
            return ResponseEntity.notFound().build();  // 404 Not Found if ID doesn't exist
        }
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

    @PostMapping("/upload-db/{id}")
    public ResponseEntity<String> uploadPdfToDb(@PathVariable int id, @RequestParam("file") MultipartFile file) {
        try {
            service.uploadInsuranceDocument(id, file);
            return ResponseEntity.ok("PDF uploaded to database successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload: " + e.getMessage());
        }
    }

}
