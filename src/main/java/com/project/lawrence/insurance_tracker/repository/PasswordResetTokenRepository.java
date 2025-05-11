package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.PasswordResetToken;
import com.project.lawrence.insurance_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    void deleteByUser(User user);
}
