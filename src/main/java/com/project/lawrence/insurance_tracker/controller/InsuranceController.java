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
            return ResponseEntity.ok(insurance);  // Return 200 OK
        } else {
            return ResponseEntity.notFound().build();  // 404 Not Found
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Insurance> addInsurance(@RequestBody Insurance insurance){
        Insurance savedInsurance = service.addInsurance(insurance);
        return new ResponseEntity<>(savedInsurance, HttpStatus.CREATED);
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

}
