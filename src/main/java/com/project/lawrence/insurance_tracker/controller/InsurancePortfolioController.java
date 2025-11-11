package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.constants.ApiPaths;
import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import com.project.lawrence.insurance_tracker.service.InsurancePortfolioAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.INSURANCE_PORTFOLIO_BASE_PATH)
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class InsurancePortfolioController {

    private final InsurancePortfolioAnalysisService portfolioAnalysisService;

    @PostMapping("/scan")
    public Map<String, Object> getPortfolio(@RequestBody List<InsuranceDTO> insuranceList) {
        System.out.println(insuranceList);
       // return portfolioAnalysisService.analyzePortfolio(insuranceList);
        return portfolioAnalysisService.aiAnalyzeportfolio(insuranceList);
    }
}
