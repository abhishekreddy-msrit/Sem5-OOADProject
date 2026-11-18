package com.rit.researchflow.application.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.domain.user.User;

/**
 * Application entry for public registration. Administrator creation is not exposed here.
 */
@Service
public class RegistrationService {

    private final StudentUserCreator studentUserCreator;
    private final FacultyUserCreator facultyUserCreator;

    public RegistrationService(StudentUserCreator studentUserCreator, FacultyUserCreator facultyUserCreator) {
        this.studentUserCreator = studentUserCreator;
        this.facultyUserCreator = facultyUserCreator;
    }

    @Transactional
    public User registerStudent(RegisterUserCommand command) {
        return studentUserCreator.register(command);
    }

    @Transactional
    public User registerFaculty(RegisterUserCommand command) {
        return facultyUserCreator.register(command);
    }
}
