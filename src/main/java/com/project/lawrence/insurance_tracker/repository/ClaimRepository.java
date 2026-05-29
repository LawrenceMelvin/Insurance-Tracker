package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Integer> {
    List<Claim> findByUserId(int userId);
}
