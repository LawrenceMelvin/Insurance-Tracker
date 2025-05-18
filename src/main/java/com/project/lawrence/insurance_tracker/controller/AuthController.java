package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.Utils.JwtTokenUtils;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.AuthService;
import com.project.lawrence.insurance_tracker.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerData) {
        logger.info("Registering user with email: {}", registerData.get("userEmail"));
        try {
            logger.info("try to register user");
            User existingUser = userRepository.findByUserEmail(registerData.get("userEmail")).orElse(null);;

            if (existingUser != null) {
                logger.info("User already exists: {}", existingUser.getUserEmail());
               if(existingUser.isVerified()){
                   return ResponseEntity.badRequest().body(Map.of(
                           "message", "Email already registered",
                           "status", "error"
                   ));
               } else {
                   String verificationToken = JwtTokenUtils.generateToken(registerData.get("userEmail"));
                   existingUser.setVerificationToken(verificationToken);
                   userRepository.save(existingUser);
                   //send Email
                   //
                   emailService.sendVerificationEmail(existingUser.getUserEmail(), verificationToken);
                   return ResponseEntity.ok().body(Map.of(
                           "message", "Verification email resent",
                           "status", "success"
                   ));
               }
            }
            logger.info("This user is a new user");
            String verificationToken = JwtTokenUtils.generateToken(registerData.get("userEmail"));
            User user = new User();
            user.setVerificationToken(verificationToken);
            user.setUserName(registerData.get("userName"));
            user.setUserEmail(registerData.get("userEmail"));
            user.setUserPassword(registerData.get("userPassword"));
            authService.registerUser(user);
            //Send Email
            emailService.sendVerificationEmail(user.getUserEmail(), verificationToken);
            logger.info("Email sent to: {}", user.getUserEmail());
            return ResponseEntity.ok().body(Map.of(
                    "message", "Registration successful",
                    "status", "success"
            ));
        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage());
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

