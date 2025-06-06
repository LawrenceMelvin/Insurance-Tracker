package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.service.EmailService;
import com.project.lawrence.insurance_tracker.service.InsuranceReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InsuranceReminderController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private InsuranceReminderService insuranceReminderService;

    @PostMapping("/schedule")
    public ResponseEntity<String> sendInsuranceReminder() {
        try {
            insuranceReminderService.findExpiry();
            return ResponseEntity.ok("Reminder email sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send reminder email: " + e.getMessage());
        }
    }
}
