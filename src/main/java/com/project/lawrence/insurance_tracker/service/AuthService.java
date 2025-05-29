package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Boolean login(String email, String password) {
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if (user != null && passwordEncoder.matches(password, user.getUserPassword())) {
            return true; // Return user if credentials are valid
        }
        return false; // Invalid credentials
    }

    public void registerUser(User user) {
        user.setUserEmail(user.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword())); // Encrypt password
        user.setRole("ROLE_USER"); // Default role
        userRepository.save(user);
    }

    public void updatePassword(User user, String newPassword) {
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
