package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.security.SecurityConfig;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import com.project.lawrence.insurance_tracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "frontend.url=http://localhost:3000")
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private InsuranceService insuranceService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    public void home_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void home_Authenticated_ReturnsInsuranceList() throws Exception {
        User user = new User();
        user.setUserEmail("test@example.com");

        InsuranceDTO insuranceDTO = new InsuranceDTO();
        insuranceDTO.setInsuranceId(123);
        insuranceDTO.setInsuranceName("HealthGuard");
        insuranceDTO.setInsuranceType("health");

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(user));
        when(insuranceService.getPersonalInsuranceByUser(any(User.class))).thenReturn(Collections.emptyList());
        when(insuranceService.mapToDTOList(anyList())).thenReturn(Collections.singletonList(insuranceDTO));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].insuranceId").value(123))
                .andExpect(jsonPath("$[0].insuranceName").value("HealthGuard"))
                .andExpect(jsonPath("$[0].insuranceType").value("health"));
    }
}
