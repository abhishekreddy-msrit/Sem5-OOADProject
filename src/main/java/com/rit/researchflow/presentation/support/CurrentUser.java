package com.rit.researchflow.presentation.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rit.researchflow.application.exception.ApplicationException;
import com.rit.researchflow.infrastructure.security.ResearchFlowUserDetails;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static ResearchFlowUserDetails require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ResearchFlowUserDetails details)) {
            throw new ApplicationException("You must be logged in");
        }
        return details;
    }

    public static Long id() {
        return require().getUserId();
    }

    public static String role() {
        return require().getRoleName();
    }
}
