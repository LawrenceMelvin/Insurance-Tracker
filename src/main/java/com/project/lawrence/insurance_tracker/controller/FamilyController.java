package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.FamilyGroup;
import com.project.lawrence.insurance_tracker.model.FamilyMemberProfile;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.repository.FamilyGroupRepository;
import com.project.lawrence.insurance_tracker.repository.FamilyMemberProfileRepository;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/family")
public class FamilyController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    @Autowired
    private FamilyMemberProfileRepository familyMemberProfileRepository;

    @Autowired
    private Insurancerepo insuranceRepo;

    @Autowired
    private InsuranceService insuranceService;

    @PostMapping("/create")
    public ResponseEntity<?> createFamilyGroup(@RequestBody Map<String, String> body, Authentication authentication) {
        String groupName = body.get("groupName");
        if (groupName == null || groupName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group name is required"));
        }
        String email = authentication.getName();
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getFamilyGroup() != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already belongs to a family group"));
        }
        FamilyGroup group = new FamilyGroup();
        group.setGroupName(groupName);
        group = familyGroupRepository.save(group);

        user.setFamilyGroup(group);
        user.setFamilyRole("ADMIN");
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Family group created successfully", "familyGroupId", group.getFamilyGroupId()));
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody Map<String, String> body, Authentication authentication) {
        String inviteeEmail = body.get("email");
        if (inviteeEmail == null || inviteeEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        String email = authentication.getName();
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        FamilyGroup group = user.getFamilyGroup();
        if (group == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "You must create a family group first"));
        }
        User invitee = userRepository.findByUserEmail(inviteeEmail).orElse(null);
        if (invitee == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Invited user not found. They must register an account first."));
        }
        if (invitee.getFamilyGroup() != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already belongs to a family group"));
        }
        invitee.setFamilyGroup(group);
        invitee.setFamilyRole("MEMBER");
        userRepository.save(invitee);

        return ResponseEntity.ok(Map.of("message", "User added to family group successfully"));
    }

    @PostMapping("/profiles/add")
    public ResponseEntity<?> addProfile(@RequestBody Map<String, String> body, Authentication authentication) {
        String fullName = body.get("fullName");
        String relationship = body.get("relationship");
        String dobStr = body.get("dateOfBirth");

        if (fullName == null || fullName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Full name is required"));
        }
        String email = authentication.getName();
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        FamilyGroup group = user.getFamilyGroup();
        if (group == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "You must create a family group first"));
        }

        FamilyMemberProfile profile = new FamilyMemberProfile();
        profile.setFullName(fullName);
        profile.setRelationship(relationship);
        profile.setFamilyGroup(group);
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                profile.setDateOfBirth(java.time.LocalDate.parse(dobStr));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid date format. Use YYYY-MM-DD."));
            }
        }
        profile = familyMemberProfileRepository.save(profile);

        return ResponseEntity.ok(Map.of("message", "Virtual family profile added successfully", "profileId", profile.getProfileId()));
    }

    @GetMapping("/members")
    public ResponseEntity<?> getFamilyMembers(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        FamilyGroup group = user.getFamilyGroup();
        if (group == null) {
            return ResponseEntity.ok(Map.of(
                    "inFamily", false,
                    "members", List.of(),
                    "profiles", List.of()
            ));
        }

        List<User> users = userRepository.findByFamilyGroup(group);
        List<FamilyMemberProfile> profiles = familyMemberProfileRepository.findByFamilyGroup(group);

        List<Map<String, Object>> mappedMembers = users.stream().map(u -> Map.<String, Object>of(
                "email", u.getUserEmail(),
                "role", u.getFamilyRole() != null ? u.getFamilyRole() : "MEMBER",
                "isVirtual", false
        )).collect(Collectors.toList());

        List<Map<String, Object>> mappedProfiles = profiles.stream().map(p -> Map.<String, Object>of(
                "profileId", p.getProfileId(),
                "fullName", p.getFullName(),
                "relationship", p.getRelationship() != null ? p.getRelationship() : "",
                "dateOfBirth", p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "",
                "isVirtual", true
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "inFamily", true,
                "groupName", group.getGroupName(),
                "familyGroupId", group.getFamilyGroupId(),
                "members", mappedMembers,
                "profiles", mappedProfiles
        ));
    }

    @GetMapping("/insurance")
    public ResponseEntity<?> getFamilyInsurance(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        FamilyGroup group = user.getFamilyGroup();
        if (group == null) {
            List<Insurance> ownInsurances = insuranceService.getInsuranceByUser(user);
            return ResponseEntity.ok(insuranceService.mapToDTOList(ownInsurances));
        }

        List<Insurance> familyInsurances = insuranceRepo.findByFamilyGroup(group);
        return ResponseEntity.ok(insuranceService.mapToDTOList(familyInsurances));
    }
}
