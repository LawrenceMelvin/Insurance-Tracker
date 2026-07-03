package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Claim;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.ClaimDocumentRepository;
import com.project.lawrence.insurance_tracker.repository.ClaimRepository;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.security.SecurityConfig;
import com.project.lawrence.insurance_tracker.service.ClaimAssistantService;
import com.project.lawrence.insurance_tracker.service.GcsStorageService;
import com.project.lawrence.insurance_tracker.service.PdfBundlerService;
import com.project.lawrence.insurance_tracker.service.UserService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "frontend.url=http://localhost:3000")
public class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ClaimRepository claimRepository;

    @MockitoBean
    private ClaimDocumentRepository claimDocumentRepository;

    @MockitoBean
    private GcsStorageService storageService;

    @MockitoBean
    private ClaimAssistantService claimAssistantService;

    @MockitoBean
    private PdfBundlerService pdfBundlerService;

    @Test
    @WithMockUser(username = "claimant@example.com")
    public void getClaims_Success() throws Exception {
        User user = new User();
        user.setUserId(5);
        user.setUserEmail("claimant@example.com");

        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setUserId(5);
        claim.setInsurerName("HDFC Ergo");

        when(userRepository.findByUserEmail("claimant@example.com")).thenReturn(Optional.of(user));
        when(claimRepository.findByUserId(5)).thenReturn(Collections.singletonList(claim));

        mockMvc.perform(get("/api/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claimId").value(1))
                .andExpect(jsonPath("$[0].insurerName").value("HDFC Ergo"));
    }

    @Test
    @WithMockUser(username = "claimant@example.com")
    public void getClaimDetails_Forbidden_Returns403() throws Exception {
        User user = new User();
        user.setUserId(5);
        user.setUserEmail("claimant@example.com");

        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setUserId(99); // different user ID

        when(userRepository.findByUserEmail("claimant@example.com")).thenReturn(Optional.of(user));
        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        mockMvc.perform(get("/api/claims/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "claimant@example.com")
    public void getChecklist_Success() throws Exception {
        ClaimAssistantService.ClaimChecklistRecord record = new ClaimAssistantService.ClaimChecklistRecord(
                List.of("Discharge Card"),
                List.of("Verify stamp")
        );

        when(claimAssistantService.generateChecklist("Star Health", "Appendectomy")).thenReturn(record);

        mockMvc.perform(get("/api/claims/checklist")
                .param("insurerName", "Star Health")
                .param("medicalEvent", "Appendectomy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredDocuments[0]").value("Discharge Card"))
                .andExpect(jsonPath("$.tips[0]").value("Verify stamp"));
    }

    @Test
    @WithMockUser(username = "claimant@example.com")
    public void createClaim_Success() throws Exception {
        User user = new User();
        user.setUserId(5);
        user.setUserEmail("claimant@example.com");

        Claim claim = new Claim();
        claim.setClaimId(12);
        claim.setUserId(5);
        claim.setInsurerName("Star Health");

        ClaimAssistantService.ClaimChecklistRecord record = new ClaimAssistantService.ClaimChecklistRecord(
                List.of("Discharge Card"),
                List.of("Verify stamp")
        );

        when(userRepository.findByUserEmail("claimant@example.com")).thenReturn(Optional.of(user));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(claimAssistantService.generateChecklist("Star Health", "Appendectomy")).thenReturn(record);

        String payload = "{"
                + "\"insuranceId\":3,"
                + "\"belongsToName\":\"Self\","
                + "\"insurerName\":\"Star Health\","
                + "\"medicalEvent\":\"Appendectomy\","
                + "\"totalClaimedAmount\":15000.0,"
                + "\"admissionDate\":\"2026-05-10\","
                + "\"dischargeDate\":\"2026-05-12\""
                + "}";

        mockMvc.perform(post("/api/claims/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claim.claimId").value(12))
                .andExpect(jsonPath("$.checklist.requiredDocuments[0]").value("Discharge Card"));
    }

    @Test
    @WithMockUser(username = "claimant@example.com")
    public void deleteClaim_Success() throws Exception {
        User user = new User();
        user.setUserId(5);
        user.setUserEmail("claimant@example.com");

        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setUserId(5);
        claim.setGcsBundleUrl("https://storage/bundle.pdf");

        when(userRepository.findByUserEmail("claimant@example.com")).thenReturn(Optional.of(user));
        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimDocumentRepository.findByClaimId(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/api/claims/1"))
                .andExpect(status().isOk());
    }
}
