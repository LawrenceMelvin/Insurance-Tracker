package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;

import java.util.List;
import java.util.Map;

public interface InsurancePortfolioAnalysisService {

    public Map<String, String> analyzePortfolio(List<InsuranceDTO> insuranceList) ;

}
