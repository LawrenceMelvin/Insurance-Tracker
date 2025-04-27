package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Load register.html
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        authService.registerUser(user);
        model.addAttribute("message", "Registration successful");
        return "redirect:/login"; // Redirect to login page after registration
    }
}

