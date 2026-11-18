package com.rit.researchflow.infrastructure.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedStudentPathRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/proposals"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void studentCannotAccessAdminPaths() throws Exception {
        mockMvc.perform(get("/admin/proposals").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void facultyCannotAccessAdminPaths() throws Exception {
        mockMvc.perform(get("/admin/proposals").with(user("faculty").roles("FACULTY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanReachAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/proposals").with(user("admin").roles("ADMINISTRATOR")))
                .andExpect(status().isOk());
    }
}
