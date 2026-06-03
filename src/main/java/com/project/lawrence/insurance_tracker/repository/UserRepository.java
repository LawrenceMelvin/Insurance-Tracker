package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.FamilyGroup;
import com.project.lawrence.insurance_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserEmail(String userEmail);
    List<User> findByFamilyGroup(FamilyGroup familyGroup);
}
