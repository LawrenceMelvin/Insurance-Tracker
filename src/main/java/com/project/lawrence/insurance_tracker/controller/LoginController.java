package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.LoginRequest;
import com.project.lawrence.insurance_tracker.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private AuthService authService;

    @GetMapping("/auth/login")
    public ResponseEntity<String> checkLoginStatus() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        logger.info("Login attempt for email: {}", request.getEmail());

        if (authService.login(request.getEmail(), request.getPassword())) {
            session.setAttribute("userEmail", request.getEmail()); // Store user in session
            logger.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "status", "success"
            ));
        }
        logger.warn("Failed login attempt for email: {}", request.getEmail());
        return ResponseEntity.badRequest().body(
                Map.of(
                        "message", "Invalid email or password",
                        "status", "error"
                ));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(Principal principal) {
        if (principal == null) {
            logger.warn("No authenticated user found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("User info requested for: {}", principal.getName());
        return ResponseEntity.ok(Map.of(
                "name", principal.getName(),
                "authenticated", true
        ));
    }

}
