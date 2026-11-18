package com.rit.researchflow.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.Student;
import com.rit.researchflow.domain.user.User;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FactoryMethodRegistrationTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void studentCreatorRegistersStudentWithHashedPassword() {
        String rawPassword = "StudentPass1";
        User user = registrationService.registerStudent(new RegisterUserCommand(
                "factory_student_" + System.nanoTime(),
                rawPassword,
                "Factory Student",
                "factory.student@example.com",
                "CSE",
                "1MS21CS999"));

        assertInstanceOf(Student.class, user);
        assertEquals("STUDENT", user.getRoleName());
        assertNotEquals(rawPassword, user.getPasswordHash());
        assertTrue(passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    @Test
    void facultyCreatorRegistersFacultyWithHashedPassword() {
        String rawPassword = "FacultyPass1";
        User user = registrationService.registerFaculty(new RegisterUserCommand(
                "factory_faculty_" + System.nanoTime(),
                rawPassword,
                "Factory Faculty",
                "factory.faculty@example.com",
                "CSE",
                "Professor"));

        assertInstanceOf(Faculty.class, user);
        assertEquals("FACULTY", user.getRoleName());
        assertTrue(passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }
}
