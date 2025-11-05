package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        Map<String, Object> result = new HashMap<>();

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
        boolean hasHealth = insuranceDTOList.stream()
                .anyMatch(i -> "HEALTH".equalsIgnoreCase(i.getInsuranceType()));
        boolean hasLife = insuranceDTOList.stream()
                .anyMatch(i -> "LIFE".equalsIgnoreCase(i.getInsuranceType()));

        String rating ="";
        String suggestion ="";

        if (!hasHealth && !hasLife) {
            //rating = "Bad";
            suggestion = "Add both Health and Life insurance to your portfolio.";
        } else if (!hasHealth) {
            //rating = "Bad";
            suggestion = "Add Health insurance to your portfolio.";
        } else if (!hasLife) {
            //rating = "Bad";
            suggestion = "Add Life insurance to your portfolio.";
        }
        
        System.out.println(suggestion);

        // Prompt
        String prompt =  """
                You will be given user insurance portfolio, you have to 
                suggest three improvements for the user based on the details below 
                and the suggestions provided.
                """
                + suggestion
                + insuranceText
                +"Display the result as Suggestion, and give a overall rating as rating= Bad or Average or Good" +
                " for user insurance portfolio";

        // Call Spring AI client
        String aiReply = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        System.out.println(aiReply);

        List<String> suggestions = new ArrayList<>();
        Pattern suggestionPattern = Pattern.compile("\\*\\*\\d+\\.\\s*\\*\\*(.*?)\\*\\*:\\s*(.*?)(?=\\n\\d+\\.|\\n\\*\\*Overall Rating\\*\\*|$)", Pattern.DOTALL);
        Pattern ratingpattern = Pattern.compile("\\*\\*Overall Rating\\*\\*:\\s*(\\w+)",Pattern.CASE_INSENSITIVE);
        Matcher suggestionMatcher = suggestionPattern.matcher(aiReply);
        while (suggestionMatcher.find()) {
            String title = suggestionMatcher.group(1).trim();
            String detail = suggestionMatcher.group(2).trim();
            suggestions.add(title + ": " + detail);
        }

        Matcher ratingmatcher = ratingpattern.matcher(aiReply);
        if (ratingmatcher.find()){
            rating = ratingmatcher.group(1).trim();
        }

        System.out.println(suggestions);
        System.out.println(rating);
        result.put("suggestion",suggestions);
        result.put("rating",rating);

//        System.out.println(aiReply);

        return result;
    }


}
