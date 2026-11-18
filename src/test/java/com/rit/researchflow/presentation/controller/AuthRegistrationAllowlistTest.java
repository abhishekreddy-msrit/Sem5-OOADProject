package com.rit.researchflow.presentation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRegistrationAllowlistTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void administratorRoleIsRejected() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("role", "ADMINISTRATOR")
                        .param("username", "badadmin_" + System.nanoTime())
                        .param("password", "password123")
                        .param("fullName", "Bad Admin")
                        .param("email", "bad.admin@example.com")
                        .param("department", "CSE")
                        .param("identifierOrDesignation", "Office"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "role"));
    }

    @Test
    void unknownRoleIsRejectedInsteadOfFallingThroughToStudent() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("role", "GUEST")
                        .param("username", "guestuser_" + System.nanoTime())
                        .param("password", "password123")
                        .param("fullName", "Guest User")
                        .param("email", "guest@example.com")
                        .param("department", "CSE")
                        .param("identifierOrDesignation", "TEMP"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "role"));
    }

    @Test
    void studentRoleIsAccepted() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("role", "STUDENT")
                        .param("username", "okstudent_" + System.nanoTime())
                        .param("password", "password123")
                        .param("fullName", "Ok Student")
                        .param("email", "ok.student@example.com")
                        .param("department", "CSE")
                        .param("identifierOrDesignation", "USN-OK"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
