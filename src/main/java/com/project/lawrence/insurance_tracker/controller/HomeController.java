package com.project.lawrence.insurance_tracker.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class HomeController {

    @RequestMapping("/")
    public String home(){
        return "Home Page";
    }


}
