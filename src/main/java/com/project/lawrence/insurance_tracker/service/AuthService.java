package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Boolean login(String username, String password) {
        User user = userRepository.findByUserName(username);
        if (user != null && passwordEncoder.matches(password, user.getUserPassword())) {
            return true; // Return user if credentials are valid
        }
        return false; // Invalid credentials
    }

    public void registerUser(User user) {
        user.setUserName(user.getUserName());
        user.setUserEmail(user.getUserEmail());
        System.out.println(user.getUserId() + " " + user.getUserName() + " " + user.getUserEmail() + " " + user.getUserPassword());
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword())); // Encrypt password
        user.setRole("ROLE_USER"); // Default role
        userRepository.save(user);
    }
}
