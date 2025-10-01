package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InsurancePortfolioAnalysisServiceImpl implements InsurancePortfolioAnalysisService {

    private final ChatClient chatClient;

    // With starter, ChatClient.Builder is auto-configured
    public InsurancePortfolioAnalysisServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> analyzePortfolio(List<InsuranceDTO> insuranceList) {
        Map<String, Object> result = new HashMap<>();
        List<String> coverageGaps = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        System.out.println("Portfolio Scanner");
        boolean hasHealth = insuranceList.stream()
                .anyMatch(i -> "HEALTH".equalsIgnoreCase(i.getInsuranceType()));
        boolean hasLife = insuranceList.stream()
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
            int totalCoverage = insuranceList.stream().mapToInt(InsuranceDTO::getInsuranceCoverage).sum();
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

        result.put("overallRating", rating);
        result.put("score", "Good".equals(rating) ? 100 : "Average".equals(rating) ? 50 : 0);
        result.put("suggestions", List.of(suggestion));

        if (!hasHealth) coverageGaps.add("Missing Health insurance");
        if (!hasLife) coverageGaps.add("Missing Life insurance");
        //if (hasHealth && hasLife && totalCoverage < 100000) coverageGaps.add("Total coverage below ₹1,00,000");

        if (hasHealth) strengths.add("Has Health insurance");
        if (hasLife) strengths.add("Has Life insurance");
        //if (totalCoverage >= 300000) strengths.add("High total coverage");

        result.put("coverageGaps", coverageGaps);
        result.put("strengths", strengths);

        System.out.println(result);

        return result;
    }

    @Override
    public Map<String, Object> aiAnalyzeportfolio(List<InsuranceDTO> insuranceDTOList) {

        String insuranceText = insuranceDTOList.stream().map(
                p -> String.format(
                        "%s: coverage=%s, premium=%s, dob=%s, start=%s, expiry=%s",
                        p.getInsuranceType(),
                        p.getInsuranceCoverage(),
                        p.getInsurancePrice(),
                        p.getDateOfBirth(),
                        p.getInsuranceFromDate(),
                        p.getInsuranceToDate()
                )
        ).collect(Collectors.joining("\n"));

        System.out.println(insuranceText);

        // Prompt
        String prompt = """
                You are an expert insurance advisor. 
                Produce a short (3-6 sentence) summary for the user that includes:
                1) overall rating (BAD/AVERAGE/GOOD) with a one-line explanation,
                2) top 3 improvements prioritized,
                3) one positive comment.
                
                Use the following structured information:

                """ + insuranceText;

        // Call Spring AI client
        String aiReply = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        System.out.println(aiReply);

        return Map.of("analysis", aiReply);
    }


}
