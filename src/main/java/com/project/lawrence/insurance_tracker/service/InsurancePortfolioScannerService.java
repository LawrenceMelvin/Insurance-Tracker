package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InsurancePortfolioScannerService {

    public Map<String, String> scanPortfolio(List<Insurance> insurances) {
        boolean hasHealth = insurances.stream()
                .anyMatch(i -> "HEALTH".equalsIgnoreCase(i.getInsuranceType()));
        boolean hasLife = insurances.stream()
                .anyMatch(i -> "LIFE".equalsIgnoreCase(i.getInsuranceType()));

        String rating;
        String suggestion;

        if (!hasHealth && !hasLife) {
            rating = "Bad";
            suggestion = "Add both Health and Life insurance to your portfolio.";
        } else if (!hasHealth) {
            rating = "Bad";
            suggestion = "Add Health insurance to your portfolio.";
        } else if (!hasLife) {
            rating = "Bad";
            suggestion = "Add Life insurance to your portfolio.";
        } else {
            int totalCoverage = insurances.stream().mapToInt(Insurance::getInsuranceCoverage).sum();
            if (totalCoverage < 100000) {
                rating = "Bad";
                suggestion = "Increase your total insurance coverage.";
            } else if (totalCoverage < 300000) {
                rating = "Average";
                suggestion = "Your portfolio is average. Consider increasing coverage.";
            } else {
                rating = "Good";
                suggestion = "Your portfolio is well diversified and covered.";
            }
        }

        return Map.of(
                "rating", rating,
                "suggestion", suggestion
        );
    }
}
