package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Integer> {
    List<ClaimDocument> findByClaimId(int claimId);
}
