package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.service.PolicyExplainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/insurance")
public class PolicyExplainerController {

    @Autowired
    private PolicyExplainerService policyExplainerService;

    @PostMapping("/explain")
    public ResponseEntity<?> explainPolicy(@RequestParam("file") MultipartFile file, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized access"));
        }

        try {
            String analysisResultJson = policyExplainerService.explainPolicy(file);
            // Returns the JSON string directly since Gemini output is structured JSON.
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(analysisResultJson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to explain policy document: " + e.getMessage()));
        }
    }
}
