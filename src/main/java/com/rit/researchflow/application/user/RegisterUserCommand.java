package com.rit.researchflow.application.user;

/**
 * Shared registration input for Student and Faculty creators.
 */
public record RegisterUserCommand(
        String username,
        String rawPassword,
        String fullName,
        String email,
        String department,
        String identifierOrDesignation
) {
}
