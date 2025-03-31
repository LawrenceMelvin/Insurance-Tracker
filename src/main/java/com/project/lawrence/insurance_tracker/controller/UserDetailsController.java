package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


public class UserDetailsController {

    @Autowired
    UserService service;

    // This class will handle user details related operations
    // such as fetching user details, updating user information, etc.

    // Example method to fetch user details
     @GetMapping("/user/{name}")
     public ResponseEntity<User> getUserDetails(@PathVariable String name) {
         return ResponseEntity.ok((User) service.loadUserByUsername(name));
     }
}
