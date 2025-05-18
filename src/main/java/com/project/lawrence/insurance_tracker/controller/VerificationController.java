package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.Utils.JwtTokenUtils;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @GetMapping("/auth/register/verify")
    public ResponseEntity verifyEmail(@RequestParam String token) {
        String emailString = jwtTokenUtils.extractEmail(token);
        User user = userRepository.findByUserEmail(emailString).orElse(null);
        if (user == null || user.getVerificationToken() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired");
        }

        if (!jwtTokenUtils.validateToken(token) || !user.getVerificationToken().equals((token))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired");
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return ResponseEntity.ok("Email verified successfully");
    }


}
