package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Insurancerepo extends JpaRepository<Insurance,Integer> {

}
