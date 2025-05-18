package com.project.lawrence.insurance_tracker.controller;


import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.AuthService;
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        // Use the email value here
        // ...
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (authService.validatePasswordResetToken(token)) {
            model.addAttribute("token", token);
            return "resetPassword";
        }
        return "redirect:/login?error=Invalid token";
    }

    @PostMapping("/reset")
    public String handlePasswordReset(@RequestParam("token") String token,
                                      @RequestParam("password") String password) {
        authService.resetPassword(token, password);
        return "redirect:/login?message=Password reset successful";
    }
}
