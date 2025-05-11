package com.project.lawrence.insurance_tracker.controller;


import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showForgotPasswordForm() {
        return "forgotPassword";
    }

    @PostMapping
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userRepository.findByUserEmail(email);
        if (user != null) {
            String token = UUID.randomUUID().toString();
            authService.createPasswordResetTokenForUser(user, token);
            // Send email with reset link (implement email service separately)
            model.addAttribute("message", "Reset link sent to your email");
            model.addAttribute("token", token);
        } else {
            model.addAttribute("error", "Email not found");
        }
        return "forgotPassword";
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
