package com.project.lawrence.insurance_tracker.repository;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Insurancerepo extends JpaRepository<Insurance,Integer> {

    // ✅ Search by Insurance Name (Case Insensitive)
    List<Insurance> findByInsuranceNameContainingIgnoreCase(String name);

    // ✅ Search by Insurance Type
    List<Insurance> findByInsuranceTypeIgnoreCase(String type);

    // ✅ Search by Price Range
    List<Insurance> findByInsurancePriceBetween(int minPrice, int maxPrice);

    List<Insurance> findByUser(User user);
}
