package com.project.lawrence.insurance_tracker.model;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private User user;
    @Column
    private LocalDateTime expiryDate;

    public PasswordResetToken() {
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }

    public PasswordResetToken(String token, User user) {
        this();
        this.token = token;
        this.user = user;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
