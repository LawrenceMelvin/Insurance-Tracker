package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InsurancePortfolioAnalysisServiceImpl implements InsurancePortfolioAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(InsurancePortfolioAnalysisServiceImpl.class);
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
        Map<String, Object> result;

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

        String prompt =  """
            You will be given a user's insurance portfolio. Output exactly four labeled sections and nothing else:
            Suggestions:, Coverage Gaps:, Strengths:, Overall Rating:
        
            - Under \\"Suggestions:\\" provide 3 improvement items (numbered or bulleted).
            - Under \\"Coverage Gaps:\\" list any missing policies or shortfalls.
            - Under \\"Strengths:\\" list positive aspects of the portfolio.
            - Under \\"Overall Rating:\\" give one of: Bad, Average, Good (single-line).
        
            Use plain text (no extra commentary). Example headers may be bolded or plain; parser will handle both.
            """ + "\n\nCurrent portfolio:\n" + insuranceText + "\n";

        String aiReply = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        logger.info("AI Reply: {}", aiReply);
        result = getParsedAiResponse(aiReply);

        logger.info("AI Analysis Result: {}", result);

        return result;
    }

    private Map<String, Object> getParsedAiResponse(String aiResponse) {
        Map<String, Object> result = new HashMap<>();
        if (aiResponse == null || aiResponse.isBlank()) {
            result.put("overallRating", "");
            result.put("score", 0);
            result.put("suggestions", List.of());
            result.put("coverageGaps", List.of());
            result.put("strengths", List.of());
            return result;
        }

        String normalized = aiResponse.replace("\r\n", "\n");

        Pattern ratingPattern = Pattern.compile("(?i)(?:\\*\\*)?overall\\s+rating(?:\\*\\*)?\\s*[:\\-]?\\s*(Bad|Average|Good)\\b");
        Pattern headerPattern = Pattern.compile("(?i)(?:\\*\\*)?(Suggestions|Suggestion|Coverage\\s*Gaps|Coverage\\s*Gap|Strengths|Strength)\\b(?:\\*\\*)?\\s*[:\\-]?");

        Matcher rMatcher = ratingPattern.matcher(normalized);
        String rating = "";
        if (rMatcher.find()) {
            rating = capitalize(rMatcher.group(1));
        } else {
            Matcher anyRating = Pattern.compile("\\b(Bad|Average|Good)\\b", Pattern.CASE_INSENSITIVE).matcher(normalized);
            if (anyRating.find()) rating = capitalize(anyRating.group(1));
        }

        Map<String, Integer> headerStart = new HashMap<>();
        Map<String, Integer> headerEnd = new HashMap<>();
        Matcher hMatcher = headerPattern.matcher(normalized);
        while (hMatcher.find()) {
            String name = hMatcher.group(1).toLowerCase().replaceAll("\\s+", " ");
            headerStart.put(name, hMatcher.start());
            headerEnd.put(name, hMatcher.end());
        }

        java.util.function.Function<String, String> extractBlock = (hdr) -> {
            Integer start = headerEnd.get(hdr.toLowerCase());
            if (start == null) return "";
            int end = normalized.length();
            Integer hdrStart = headerStart.get(hdr.toLowerCase());
            if (hdrStart == null) hdrStart = 0;
            for (Map.Entry<String, Integer> e : headerStart.entrySet()) {
                if (!e.getKey().equalsIgnoreCase(hdr) && e.getValue() > hdrStart) {
                    // stop before the start of the next header
                    end = Math.min(end, e.getValue());
                }
            }
            int ratingIdx = normalized.toLowerCase().indexOf("overall rating");
            if (ratingIdx > -1) end = Math.min(end, ratingIdx);
            if (end < start) return "";
            return normalized.substring(start, end).trim();
        };

        java.util.function.Function<String, List<String>> parseItems = (block) -> {
            List<String> items = new ArrayList<>();
            if (block == null || block.isBlank()) return items;
            Pattern itemPattern = Pattern.compile("(?m)^(?:\\s*\\d+\\.|\\s*[\\-*\\u2022])\\s*(.+)$");
            Matcher m = itemPattern.matcher(block);
            while (m.find()) {
                String it = sanitizeItem(m.group(1));
                if (it != null && !it.isEmpty()) items.add(it);
            }
            if (!items.isEmpty()) return items;
            String[] parts = block.split("\\n\\n+");
            for (String p : parts) {
                String cleaned = p.replaceAll("\\*\\*", "").trim();
                if (!cleaned.isEmpty()) {
                    cleaned = cleaned.replaceFirst("^\\s*\\d+\\.\\s*", "");
                    String s = sanitizeItem(cleaned);
                    if (s != null && !s.isEmpty()) items.add(s);
                }
            }
            if (items.isEmpty() && !block.isBlank()) {
                String s = sanitizeItem(block);
                if (s != null && !s.isEmpty()) items.add(s);
            }
            return items;
        };

        List<String> suggestions = parseItems.apply(extractBlock.apply("suggestions"));
        if (suggestions.isEmpty()) {
            Pattern broadItem = Pattern.compile("(?m)^(?:\\s*\\d+\\.|\\s*[\\-*\\u2022])\\s*(.+)$");
            Matcher bm = broadItem.matcher(normalized);
            while (bm.find()) {
                String it = sanitizeItem(bm.group(1));
                if (it != null && !it.isEmpty()) suggestions.add(it);
            }
        }

        List<String> coverageGaps = parseItems.apply(extractBlock.apply("coverage gaps"));
        List<String> strengths = parseItems.apply(extractBlock.apply("strengths"));

        if (coverageGaps.isEmpty() || strengths.isEmpty()) {
            String lower = normalized.toLowerCase();
            if (coverageGaps.isEmpty()) {
                if (lower.contains("missing health") || lower.contains("missing health insurance")) coverageGaps.add("Missing Health insurance");
                if (lower.contains("missing life") || lower.contains("missing life insurance")) coverageGaps.add("Missing Life insurance");
                if (lower.contains("coverage below") || lower.contains("total coverage below")) coverageGaps.add("Total coverage may be insufficient");
            }
            if (strengths.isEmpty()) {
                if (lower.contains("has health") || lower.contains("has health insurance")) strengths.add("Has Health insurance");
                if (lower.contains("has life") || lower.contains("has life insurance")) strengths.add("Has Life insurance");
                if (lower.contains("well diversified") || lower.contains("high total coverage")) strengths.add("Strong total coverage");
            }
        }

        int score = "Good".equalsIgnoreCase(rating) ? 100 : "Average".equalsIgnoreCase(rating) ? 50 : 0;

        result.put("overallRating", rating);
        result.put("score", score);
        result.put("suggestions", suggestions.isEmpty() ? List.of() : suggestions);
        result.put("coverageGaps", coverageGaps.isEmpty() ? List.of() : coverageGaps);
        result.put("strengths", strengths.isEmpty() ? List.of() : strengths);
        return result;
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        s = s.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String sanitizeItem(String s) {
        if (s == null) return null;
        // remove asterisks, collapse whitespace
        String cleaned = s.replaceAll("\\*+", "").replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) return null;
        // strip leading/trailing non-alphanumeric characters
        cleaned = cleaned.replaceFirst("^[^\\p{Alnum}]+", "").replaceFirst("[^\\p{Alnum}]+$", "");
        // remove a trailing isolated single-letter token (e.g. "..., S" -> remove "S")
        cleaned = cleaned.replaceFirst("[\\s\\p{Punct}]+[A-Za-z]$", "");
        // remove a leading isolated single-letter token (e.g. "C, Suggest..." -> remove "C")
        cleaned = cleaned.replaceFirst("^[A-Za-z][\\s\\p{Punct}]+", "");
        cleaned = cleaned.trim();
        if (cleaned.isBlank()) return null;
        return cleaned;
    }
}
