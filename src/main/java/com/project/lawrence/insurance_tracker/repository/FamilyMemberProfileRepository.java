package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.FamilyGroup;
import com.project.lawrence.insurance_tracker.model.FamilyMemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyMemberProfileRepository extends JpaRepository<FamilyMemberProfile, Integer> {
    List<FamilyMemberProfile> findByFamilyGroup(FamilyGroup familyGroup);
}
