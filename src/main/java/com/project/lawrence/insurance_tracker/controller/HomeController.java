package com.project.lawrence.insurance_tracker.controller;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.UserRepository;
import com.project.lawrence.insurance_tracker.service.InsuranceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller("/")
public class HomeController {

    @Autowired
    private InsuranceService insuranceService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model, HttpSession session){
        String username = (String) session.getAttribute("user");
        User user = userRepository.findByUserName(username);
        List<Insurance> insurance = insuranceService.getInsuranceByUser(user);
        model.addAttribute("insurance", insurance);
        return "home";
    }


}
