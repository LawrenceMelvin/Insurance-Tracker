package com.project.lawrence.insurance_tracker.controller;


import com.project.lawrence.insurance_tracker.Utils.JwtTokenUtils;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.AuthService;
import com.project.lawrence.insurance_tracker.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.UUID;

@Controller
public class ForgotPasswordController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        User existingUser = userRepository.findByUserEmail(email).orElse(null);
        // Use the email value here
        // ...
        String verificationToken = JwtTokenUtils.generateToken(email);
        assert existingUser != null;
        existingUser.setVerificationToken(verificationToken);
        userRepository.save(existingUser);
        // Send email with the verification token
        emailService.sendForgotPasswordEmail(existingUser.getUserEmail(), verificationToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        String email = JwtTokenUtils.extractEmail(token);
        User user = userRepository.findByUserEmail(email).orElse(null);
        if (user != null && user.getVerificationToken().equals(token)) {
            authService.updatePassword(user, newPassword);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid token",
                    "status", "error"
            ));
        }
    }
}
