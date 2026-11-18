package com.rit.researchflow.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rit.researchflow.application.user.RegisterUserCommand;
import com.rit.researchflow.application.user.RegistrationService;
import com.rit.researchflow.domain.exception.DomainValidationException;
import com.rit.researchflow.presentation.form.RegisterForm;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        String role = form.getRole() == null ? "" : form.getRole().trim().toUpperCase();
        if (!"STUDENT".equals(role) && !"FACULTY".equals(role)) {
            bindingResult.rejectValue(
                    "role",
                    "forbidden",
                    "Public registration allows only Student or Faculty accounts");
            return "auth/register";
        }

        RegisterUserCommand command = new RegisterUserCommand(
                form.getUsername(),
                form.getPassword(),
                form.getFullName(),
                form.getEmail(),
                form.getDepartment(),
                form.getIdentifierOrDesignation());

        try {
            if ("FACULTY".equals(role)) {
                registrationService.registerFaculty(command);
            } else {
                registrationService.registerStudent(command);
            }
        } catch (DomainValidationException ex) {
            bindingResult.reject("registration", ex.getMessage());
            return "auth/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please log in.");
        return "redirect:/login";
    }
}
