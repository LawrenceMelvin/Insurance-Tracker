package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Load login.html
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if (authService.login(username, password)) {
            session.setAttribute("user", username); // Store user in session
            return "redirect:/home";
        } else {
            model.addAttribute("user", "Invalid user or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/login";
    }
}
