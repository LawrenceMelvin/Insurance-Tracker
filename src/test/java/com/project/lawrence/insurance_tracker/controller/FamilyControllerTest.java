package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.FamilyGroup;
import com.project.lawrence.insurance_tracker.model.FamilyMemberProfile;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.FamilyGroupRepository;
import com.project.lawrence.insurance_tracker.repository.FamilyMemberProfileRepository;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.security.SecurityConfig;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamilyController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "frontend.url=http://localhost:3000")
public class FamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FamilyGroupRepository familyGroupRepository;

    @MockitoBean
    private FamilyMemberProfileRepository familyMemberProfileRepository;

    @MockitoBean
    private Insurancerepo insuranceRepo;

    @MockitoBean
    private InsuranceService insuranceService;

    @Test
    @WithMockUser(username = "admin@example.com")
    public void createFamilyGroup_Success() throws Exception {
        User user = new User();
        user.setUserEmail("admin@example.com");

        FamilyGroup group = new FamilyGroup();
        group.setFamilyGroupId(1);
        group.setGroupName("Smiths");

        when(userRepository.findByUserEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(familyGroupRepository.save(any(FamilyGroup.class))).thenReturn(group);

        mockMvc.perform(post("/family/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\":\"Smiths\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Family group created successfully"))
                .andExpect(jsonPath("$.familyGroupId").value(1));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    public void createFamilyGroup_AlreadyExists_ReturnsBadRequest() throws Exception {
        User user = new User();
        user.setUserEmail("admin@example.com");
        user.setFamilyGroup(new FamilyGroup());

        when(userRepository.findByUserEmail("admin@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/family/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\":\"Smiths\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User already belongs to a family group"));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    public void inviteUser_Success() throws Exception {
        User admin = new User();
        admin.setUserEmail("admin@example.com");
        FamilyGroup group = new FamilyGroup();
        group.setGroupName("Smiths");
        admin.setFamilyGroup(group);

        User invitee = new User();
        invitee.setUserEmail("invitee@example.com");

        when(userRepository.findByUserEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByUserEmail("invitee@example.com")).thenReturn(Optional.of(invitee));

        mockMvc.perform(post("/family/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invitee@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User added to family group successfully"));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    public void inviteUser_InviteeNotFound_ReturnsNotFound() throws Exception {
        User admin = new User();
        admin.setUserEmail("admin@example.com");
        admin.setFamilyGroup(new FamilyGroup());

        when(userRepository.findByUserEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByUserEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/family/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nonexistent@example.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invited user not found. They must register an account first."));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    public void addProfile_Success() throws Exception {
        User admin = new User();
        admin.setUserEmail("admin@example.com");
        admin.setFamilyGroup(new FamilyGroup());

        FamilyMemberProfile profile = new FamilyMemberProfile();
        profile.setProfileId(10);
        profile.setFullName("Child Smith");

        when(userRepository.findByUserEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(familyMemberProfileRepository.save(any(FamilyMemberProfile.class))).thenReturn(profile);

        mockMvc.perform(post("/family/profiles/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Child Smith\",\"relationship\":\"Child\",\"dateOfBirth\":\"2015-05-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Virtual family profile added successfully"))
                .andExpect(jsonPath("$.profileId").value(10));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    public void getFamilyMembers_NotInFamily() throws Exception {
        User user = new User();
        user.setUserEmail("user@example.com");

        when(userRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/family/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inFamily").value(false))
                .andExpect(jsonPath("$.members").isEmpty())
                .andExpect(jsonPath("$.profiles").isEmpty());
    }
}
