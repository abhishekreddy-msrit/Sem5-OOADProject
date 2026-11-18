package com.rit.researchflow.application.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Student;
import com.rit.researchflow.domain.user.User;

@Component
public class StudentUserCreator extends UserCreator {

    public StudentUserCreator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository, passwordEncoder);
    }

    @Override
    protected User createUser(RegisterUserCommand command, String passwordHash) {
        return new Student(
                command.username().trim(),
                passwordHash,
                command.fullName().trim(),
                command.email().trim(),
                command.department().trim(),
                command.identifierOrDesignation().trim());
    }
}
