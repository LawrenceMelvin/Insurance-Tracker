package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/insurance")
public class InsuranceController {

    @Autowired
    InsuranceService service;

    @GetMapping("/all")
    public ResponseEntity<List<Insurance>> getInsuranceAllName(){
        return new ResponseEntity<>(service.getInsuranceName(), HttpStatus.OK);
    }

    @GetMapping("/{insuranceId}")
    public ResponseEntity<Insurance> getById(@PathVariable int insuranceId){
        Insurance insurance = service.getInsuranceById(insuranceId);
        if (insurance != null) {
            return ResponseEntity.ok(insurance);  // ✅ Return 200 OK
        } else {
            return ResponseEntity.notFound().build();  // ❌ 404 Not Found
        }
//        return new ResponseEntity<>(service.getInsuranceById(insuranceId),HttpStatus.OK);
    }

}
