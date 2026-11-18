package com.rit.researchflow.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.application.facade.ProposalWorkflowFacade;
import com.rit.researchflow.application.user.RegistrationService;
import com.rit.researchflow.domain.repository.ResearchProposalRepository;
import com.rit.researchflow.domain.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class DemoDataSeederIdempotencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResearchProposalRepository researchProposalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ProposalWorkflowFacade proposalWorkflowFacade;

    @Test
    @Transactional
    void seedingTwiceDoesNotDuplicateUsersOrProposals() {
        long suffix = System.nanoTime();
        String adminUser = "seed_admin_" + suffix;
        String studentUser = "seed_student_" + suffix;
        String facultyUser = "seed_faculty_" + suffix;

        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_USERNAME", adminUser);
        env.setProperty("ADMIN_PASSWORD", "SeedPass123");
        env.setProperty("SEED_STUDENT_USERNAME", studentUser);
        env.setProperty("SEED_STUDENT_PASSWORD", "SeedPass123");
        env.setProperty("SEED_FACULTY_USERNAME", facultyUser);
        env.setProperty("SEED_FACULTY_PASSWORD", "SeedPass123");

        DemoDataSeeder seeder = new DemoDataSeeder(
                env,
                userRepository,
                researchProposalRepository,
                passwordEncoder,
                registrationService,
                proposalWorkflowFacade);
        // enable via reflection-free approach: property default true on component field;
        // constructed manually so set enabled using Spring's @Value won't apply — call run after setting field
        try {
            var field = DemoDataSeeder.class.getDeclaredField("seedEnabled");
            field.setAccessible(true);
            field.setBoolean(seeder, true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        seeder.run(new DefaultApplicationArguments());
        long usersAfterFirst = countUsers(adminUser, studentUser, facultyUser);
        long proposalsAfterFirst = researchProposalRepository.findByAuthorId(
                userRepository.findByUsername(studentUser).orElseThrow().getId()).size();

        seeder.run(new DefaultApplicationArguments());
        long usersAfterSecond = countUsers(adminUser, studentUser, facultyUser);
        long proposalsAfterSecond = researchProposalRepository.findByAuthorId(
                userRepository.findByUsername(studentUser).orElseThrow().getId()).size();

        assertEquals(3, usersAfterFirst);
        assertEquals(usersAfterFirst, usersAfterSecond);
        assertEquals(proposalsAfterFirst, proposalsAfterSecond);
        assertEquals(5, proposalsAfterFirst);
    }

    private long countUsers(String... usernames) {
        long count = 0;
        for (String username : usernames) {
            if (userRepository.findByUsername(username).isPresent()) {
                count++;
            }
        }
        return count;
    }
}
