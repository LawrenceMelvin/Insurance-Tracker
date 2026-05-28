package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    @Autowired
    private InsuranceService insuranceService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public List<InsuranceDTO> home(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUserEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        List<Insurance> ownInsurances = insuranceService.getPersonalInsuranceByUser(user);
        return insuranceService.mapToDTOList(ownInsurances);
    }
}
