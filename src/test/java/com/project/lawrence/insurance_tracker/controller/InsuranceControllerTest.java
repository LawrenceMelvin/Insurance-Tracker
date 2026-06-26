package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.FamilyMemberProfileRepository;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.security.SecurityConfig;
import com.project.lawrence.insurance_tracker.service.InsuranceEstimationService;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsuranceController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "frontend.url=http://localhost:3000")
public class InsuranceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsuranceService insuranceService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FamilyMemberProfileRepository familyMemberProfileRepository;

    @MockitoBean
    private InsuranceEstimationService insuranceEstimationService;

    @Test
    @WithMockUser(username = "patient@example.com")
    public void calculateEstimate_Success() throws Exception {
        User user = new User();
        user.setUserId(10);
        user.setUserEmail("patient@example.com");

        Insurance insurance = new Insurance();
        insurance.setInsuranceId(5);
        insurance.setUser(user);
        insurance.setInsuranceName("Star Health Policy");

        InsuranceEstimationService.EstimateResponse estimateResponse = new InsuranceEstimationService.EstimateResponse(
                45000.0,
                15000.0,
                Collections.singletonList(new InsuranceEstimationService.DeductionBreakdown("Room Rent", 5000.0, "Exceeded rent cap")),
                Collections.singletonList("Downgrade room")
        );

        when(userRepository.findByUserEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(insuranceService.getInsuranceById(5)).thenReturn(insurance);
        when(insuranceEstimationService.calculateEstimate(eq(insurance), any(InsuranceEstimationService.EstimateRequest.class)))
                .thenReturn(estimateResponse);

        String payload = """
                {
                  "roomRentPerDay": 7000.0,
                  "numberOfDays": 4,
                  "expectedProcedureCost": 30000.0,
                  "otherCharges": 5000.0,
                  "manualPolicyRules": {
                    "copayPercent": 10,
                    "roomRentLimit": 5000.0,
                    "deductible": 0.0
                  }
                }
                """;

        mockMvc.perform(post("/insurance/5/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedInsurerCovered").value(45000.0))
                .andExpect(jsonPath("$.estimatedUserOutOfPocket").value(15000.0))
                .andExpect(jsonPath("$.deductionsBreakdown[0].category").value("Room Rent"))
                .andExpect(jsonPath("$.suggestions[0]").value("Downgrade room"));
    }

    @Test
    @WithMockUser(username = "patient@example.com")
    public void calculateEstimate_Forbidden_DifferentUser() throws Exception {
        User user = new User();
        user.setUserId(10);
        user.setUserEmail("patient@example.com");

        User owner = new User();
        owner.setUserId(99);
        owner.setUserEmail("other@example.com");

        Insurance insurance = new Insurance();
        insurance.setInsuranceId(5);
        insurance.setUser(owner);

        when(userRepository.findByUserEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(insuranceService.getInsuranceById(5)).thenReturn(insurance);

        String payload = """
                {
                  "roomRentPerDay": 7000.0,
                  "numberOfDays": 4,
                  "expectedProcedureCost": 30000.0,
                  "otherCharges": 5000.0
                }
                """;

        mockMvc.perform(post("/insurance/5/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    public void calculateEstimate_Unauthorized_NoUser() throws Exception {
        mockMvc.perform(post("/insurance/5/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
