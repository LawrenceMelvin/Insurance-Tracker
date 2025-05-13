package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
public class LoginController {

    @GetMapping("/login")
    public ResponseEntity<String> checkLoginStatus() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public Principal getUser(Principal user) {
        return user;
    }
}
