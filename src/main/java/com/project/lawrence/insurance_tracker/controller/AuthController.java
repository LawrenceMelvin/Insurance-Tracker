package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {
        try {
            User user = new User();
            user.setUserName(request.getUserName());
            user.setUserEmail(request.getUserEmail());
            user.setUserPassword(request.getUserPassword());

            authService.registerUser(user);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Registration successful",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
            ));
        }
    }

//    @GetMapping("/check-email")
//    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
//        boolean exists = authService.emailExists(email);
//        return ResponseEntity.ok(exists);
//    }
}

