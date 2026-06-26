package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsuranceEstimationService {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceEstimationService.class);
    private final ChatClient chatClient;

    public InsuranceEstimationService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public record DeductionBreakdown(
        String category,
        Double deductedAmount,
        String reason
    ) {}

    public record EstimateResponse(
        Double estimatedInsurerCovered,
        Double estimatedUserOutOfPocket,
        List<DeductionBreakdown> deductionsBreakdown,
        List<String> suggestions
    ) {}

    public record ManualPolicyRules(
        Integer copayPercent,
        Double roomRentLimit,
        Double deductible
    ) {}

    public record EstimateRequest(
        Double roomRentPerDay,
        Integer numberOfDays,
        Double expectedProcedureCost,
        Double otherCharges,
        ManualPolicyRules manualPolicyRules
    ) {}

    public EstimateResponse calculateEstimate(Insurance insurance, EstimateRequest request) {
        String policyJsonContext = insurance.getPolicyAnalysisJson();
        String manualRulesContext = "";
        if (request.manualPolicyRules() != null) {
            manualRulesContext = String.format(
                "Manual Policy Rules fallback (use these if the policy analysis JSON doesn't cover them): Copay: %d%%, Room Rent Limit: %.2f, Deductible: %.2f",
                request.manualPolicyRules().copayPercent() != null ? request.manualPolicyRules().copayPercent() : 0,
                request.manualPolicyRules().roomRentLimit() != null ? request.manualPolicyRules().roomRentLimit() : 0.0,
                request.manualPolicyRules().deductible() != null ? request.manualPolicyRules().deductible() : 0.0
            );
        }

        String prompt = String.format("""
            You are a professional medical insurance cost estimator.
            Based on the policy terms and the upcoming hospitalization bill inputs, calculate the estimated amount the insurer will cover and the estimated out-of-pocket amount the user must pay.
            
            Hospitalization Inputs:
            - Room Rent per Day: %.2f
            - Number of Days: %d
            - Expected Procedure Cost: %.2f
            - Other Charges (admissible medicine, surgeon fees, etc.): %.2f
            
            Policy Rules Context (from AI-parsed PDF):
            %s
            
            %s
            
            Calculation Guidelines:
            1. Total Expected Bill = (Room Rent per Day * Number of Days) + Expected Procedure Cost + Other Charges.
            2. Check if the Room Rent per day exceeds the room rent limit/cap. Any excess room rent (excess room rent per day * number of days) is paid fully out-of-pocket by the user.
            3. Apply any deductibles (the amount the user must pay first before the insurer pays anything).
            4. Apply the copayment percentage (the percentage of the admissible claim amount that the user must bear). For example, if there's a 10%% copay, the user pays 10%% of the remaining admissible claim amount.
            5. The total out-of-pocket is: (Excess Room Rent) + Deductible + (Copay Amount) + any other non-admissible charges.
            6. The estimated insurer covered amount is: Total Expected Bill - Total out-of-pocket (capped at the policy's overall coverage limit if specified, else use the logical coverage).
            
            Provide a clean JSON object containing:
            1. estimatedInsurerCovered: The total amount paid by the insurer.
            2. estimatedUserOutOfPocket: The total amount paid by the user.
            3. deductionsBreakdown: A list of objects with fields:
               - category: E.g., "Room Rent", "Copay", "Deductible", "Non-admissible"
               - deductedAmount: The amount of deduction for this category.
               - reason: A short, user-friendly explanation of why it was deducted, referencing the policy terms.
            4. suggestions: Actionable tips for the user to reduce their out-of-pocket expenses (e.g. "Choosing a room under 5000/day will eliminate 8,000 in room rent deductions", "Ask if the procedure can be done in a network hospital for full coverage").
            
            Format the output strictly as a JSON object matching the requested structure.
            """,
            request.roomRentPerDay() != null ? request.roomRentPerDay() : 0.0,
            request.numberOfDays() != null ? request.numberOfDays() : 0,
            request.expectedProcedureCost() != null ? request.expectedProcedureCost() : 0.0,
            request.otherCharges() != null ? request.otherCharges() : 0.0,
            policyJsonContext != null ? policyJsonContext : "No stored policy analysis JSON available.",
            manualRulesContext
        );

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(EstimateResponse.class);
        } catch (Exception e) {
            logger.error("Error generating insurance out-of-pocket estimate via Gemini, running fallback calculation", e);
            
            // Dynamic simple fallback calculation logic if AI fails
            double roomRentLimit = 0.0;
            double copayPercent = 0.0;
            double deductible = 0.0;
            if (request.manualPolicyRules() != null) {
                if (request.manualPolicyRules().roomRentLimit() != null) roomRentLimit = request.manualPolicyRules().roomRentLimit();
                if (request.manualPolicyRules().copayPercent() != null) copayPercent = request.manualPolicyRules().copayPercent() / 100.0;
                if (request.manualPolicyRules().deductible() != null) deductible = request.manualPolicyRules().deductible();
            }

            double roomRentPerDay = request.roomRentPerDay() != null ? request.roomRentPerDay() : 0.0;
            int days = request.numberOfDays() != null ? request.numberOfDays() : 0;
            double procedureCost = request.expectedProcedureCost() != null ? request.expectedProcedureCost() : 0.0;
            double other = request.otherCharges() != null ? request.otherCharges() : 0.0;

            double totalBill = (roomRentPerDay * days) + procedureCost + other;
            double excessRoomRent = 0.0;
            if (roomRentLimit > 0 && roomRentPerDay > roomRentLimit) {
                excessRoomRent = (roomRentPerDay - roomRentLimit) * days;
            }

            double admissible = totalBill - excessRoomRent;
            double afterDeductible = Math.max(0.0, admissible - deductible);
            double deductibleDeducted = Math.min(admissible, deductible);
            double copayDeducted = afterDeductible * copayPercent;
            double insurerCovered = Math.max(0.0, afterDeductible - copayDeducted);
            double outOfPocket = totalBill - insurerCovered;

            List<DeductionBreakdown> breakdowns = new java.util.ArrayList<>();
            if (excessRoomRent > 0) {
                breakdowns.add(new DeductionBreakdown("Room Rent", excessRoomRent, "Room rent exceeded manual cap of " + roomRentLimit));
            }
            if (deductibleDeducted > 0) {
                breakdowns.add(new DeductionBreakdown("Deductible", deductibleDeducted, "Policy deductible applied first"));
            }
            if (copayDeducted > 0) {
                breakdowns.add(new DeductionBreakdown("Copay", copayDeducted, (copayPercent * 100) + "% copayment applied on admissible bill"));
            }

            return new EstimateResponse(
                insurerCovered,
                outOfPocket,
                breakdowns,
                List.of("Fallback estimation applied due to service interruption. Downgrade room rent to minimize out-of-pocket expense.")
            );
        }
    }
}
