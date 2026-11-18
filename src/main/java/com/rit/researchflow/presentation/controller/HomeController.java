package com.rit.researchflow.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.rit.researchflow.presentation.support.CurrentUser;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home() {
        return switch (CurrentUser.role()) {
            case "STUDENT" -> "redirect:/student/proposals";
            case "FACULTY" -> "redirect:/faculty/assignments";
            case "ADMINISTRATOR" -> "redirect:/admin/proposals";
            default -> "redirect:/login";
        };
    }
}
