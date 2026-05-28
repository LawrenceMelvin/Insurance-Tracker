package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.FamilyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, Integer> {
}
