package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.PasswordResetToken;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.PasswordResetTokenRepository;
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

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Transactional
    public void createPasswordResetTokenForUser(User user, String token) {
        //Delete existing token if present
        tokenRepository.deleteByUser(user);
        tokenRepository.flush();
        // Create a new token
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        tokenRepository.save(myToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired token");
        }

        User user = resetToken.getUser();
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        return resetToken != null && !resetToken.getExpiryDate().isBefore(LocalDateTime.now());
    }

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
        user.setUserName(user.getUserName());
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
