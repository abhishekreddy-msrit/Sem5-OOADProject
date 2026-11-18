package com.rit.researchflow.application.user;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.rit.researchflow.domain.exception.DomainValidationException;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.User;

/**
 * Creator in the Factory Method pattern.
 * Subclasses implement {@link #createUser(RegisterUserCommand, String)} to decide the concrete User type.
 */
public abstract class UserCreator {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    protected UserCreator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Template for registration: validate, create via factory method, persist.
     */
    public final User register(RegisterUserCommand command) {
        validate(command);
        if (userRepository.existsByUsername(command.username().trim())) {
            throw new DomainValidationException("Username is already taken");
        }
        String passwordHash = passwordEncoder.encode(command.rawPassword());
        User user = createUser(command, passwordHash);
        return userRepository.save(user);
    }

    /**
     * Factory Method — subclasses return Student or Faculty.
     */
    protected abstract User createUser(RegisterUserCommand command, String passwordHash);

    protected void validate(RegisterUserCommand command) {
        requireText(command.username(), "Username", 3, 100);
        requireText(command.rawPassword(), "Password", 8, 100);
        requireText(command.fullName(), "Full name", 2, 200);
        requireText(command.email(), "Email", 5, 255);
        requireText(command.department(), "Department", 2, 200);
        requireText(command.identifierOrDesignation(), "Identifier/designation", 2, 100);
        if (!command.email().contains("@")) {
            throw new DomainValidationException("Email must be valid");
        }
    }

    private void requireText(String value, String label, int min, int max) {
        if (value == null || value.trim().length() < min || value.trim().length() > max) {
            throw new DomainValidationException(label + " must be between " + min + " and " + max + " characters");
        }
    }
}
