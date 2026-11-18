package com.rit.researchflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.application.dto.ProposalContentCommand;
import com.rit.researchflow.application.facade.ProposalWorkflowFacade;
import com.rit.researchflow.application.user.RegisterUserCommand;
import com.rit.researchflow.application.user.RegistrationService;
import com.rit.researchflow.domain.proposal.RecommendationType;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.proposal.ReviewerAssignment;
import com.rit.researchflow.domain.repository.ResearchProposalRepository;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.Student;

/**
 * Idempotent demo seed for academic demonstration. Passwords come from environment only.
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final Environment environment;
    private final UserRepository userRepository;
    private final ResearchProposalRepository researchProposalRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationService registrationService;
    private final ProposalWorkflowFacade proposalWorkflowFacade;

    @Value("${researchflow.seed.enabled:true}")
    private boolean seedEnabled;

    public DemoDataSeeder(Environment environment,
                          UserRepository userRepository,
                          ResearchProposalRepository researchProposalRepository,
                          PasswordEncoder passwordEncoder,
                          RegistrationService registrationService,
                          ProposalWorkflowFacade proposalWorkflowFacade) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.researchProposalRepository = researchProposalRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrationService = registrationService;
        this.proposalWorkflowFacade = proposalWorkflowFacade;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Demo seed disabled");
            return;
        }

        String adminUsername = optional("ADMIN_USERNAME");
        String adminPassword = optional("ADMIN_PASSWORD");
        String studentUsername = optional("SEED_STUDENT_USERNAME");
        String studentPassword = optional("SEED_STUDENT_PASSWORD");
        String facultyUsername = optional("SEED_FACULTY_USERNAME");
        String facultyPassword = optional("SEED_FACULTY_PASSWORD");

        if (adminUsername == null || adminPassword == null
                || studentUsername == null || studentPassword == null
                || facultyUsername == null || facultyPassword == null) {
            log.warn("Demo seed skipped: set ADMIN_*, SEED_STUDENT_* and SEED_FACULTY_* in .env to enable demo accounts");
            return;
        }

        Administrator admin = ensureAdministrator(adminUsername, adminPassword);
        Student student = ensureStudent(studentUsername, studentPassword);
        Faculty faculty = ensureFaculty(facultyUsername, facultyPassword);

        if (researchProposalRepository.findByAuthorId(student.getId()).isEmpty()) {
            seedProposals(admin, student, faculty);
            log.info("Demo proposals seeded for student '{}'", studentUsername);
        } else {
            log.info("Demo proposals already present; skipping proposal seed");
        }

        log.info("Demo users ready (usernames only): admin='{}', student='{}', faculty='{}'",
                adminUsername, studentUsername, facultyUsername);
    }

    private Administrator ensureAdministrator(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (!(user instanceof Administrator administrator)) {
                        throw new IllegalStateException("Seed username exists but is not an Administrator: " + username);
                    }
                    return administrator;
                })
                .orElseGet(() -> {
                    Administrator created = new Administrator(
                            username,
                            passwordEncoder.encode(rawPassword),
                            "Demo Administrator",
                            "admin@researchflow.local",
                            "Research Office");
                    return (Administrator) userRepository.save(created);
                });
    }

    private Student ensureStudent(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (!(user instanceof Student student)) {
                        throw new IllegalStateException("Seed username exists but is not a Student: " + username);
                    }
                    return student;
                })
                .orElseGet(() -> (Student) registrationService.registerStudent(new RegisterUserCommand(
                        username,
                        rawPassword,
                        "Demo Student",
                        "student@researchflow.local",
                        "Computer Science",
                        "1MS21CS001")));
    }

    private Faculty ensureFaculty(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (!(user instanceof Faculty faculty)) {
                        throw new IllegalStateException("Seed username exists but is not a Faculty member: " + username);
                    }
                    return faculty;
                })
                .orElseGet(() -> (Faculty) registrationService.registerFaculty(new RegisterUserCommand(
                        username,
                        rawPassword,
                        "Demo Faculty",
                        "faculty@researchflow.local",
                        "Computer Science",
                        "Associate Professor")));
    }

    private void seedProposals(Administrator admin, Student student, Faculty faculty) {
        // 1) Draft
        proposalWorkflowFacade.createDraft(student.getId(), content(
                "Draft: Campus IoT Occupancy Study",
                "A draft investigation into room occupancy sensing for lecture halls using low-power IoT nodes.",
                "Establish baseline occupancy patterns across selected campus lecture rooms.",
                "Deploy battery-powered sensors and collect anonymised occupancy counts over two weeks.",
                "Expect clearer scheduling insights and reduced energy waste in unused rooms."));

        // 2) Submitted awaiting assignment
        ResearchProposal submitted = proposalWorkflowFacade.createDraft(student.getId(), content(
                "Submitted: Secure Student Feedback Portal",
                "This proposal outlines a secure portal for collecting structured course feedback from students.",
                "Improve feedback quality while protecting student anonymity and institutional integrity.",
                "Build a role-aware web application with audited submission workflows and review queues.",
                "Expect higher response rates and cleaner feedback analytics for departments."));
        proposalWorkflowFacade.submitProposal(submitted.getId(), student.getId());

        // 3) Under review with pending faculty review
        ResearchProposal underReview = proposalWorkflowFacade.createDraft(student.getId(), content(
                "Under Review: Adaptive Quiz Recommendation",
                "The study proposes adaptive quiz recommendations driven by recent learner performance signals.",
                "Help learners practise weaker topics through personalised quiz sequencing.",
                "Prototype a recommender and evaluate it with a controlled cohort of undergraduate students.",
                "Expect improved practice completion and measurable topic-mastery gains."));
        underReview = proposalWorkflowFacade.submitProposal(underReview.getId(), student.getId());
        proposalWorkflowFacade.assignReviewer(underReview.getId(), faculty.getId(), admin.getId());

        // 4) Revision required with prior review/decision history
        ResearchProposal revision = proposalWorkflowFacade.createDraft(student.getId(), content(
                "Revision Required: Lab Equipment Booking System",
                "A booking system for shared laboratory equipment with conflict detection and approval trails.",
                "Reduce booking conflicts and improve utilisation of scarce laboratory resources.",
                "Design reservation workflows with faculty approval and audit-friendly status history.",
                "Expect fewer clashes and transparent booking outcomes for students and staff."));
        revision = proposalWorkflowFacade.submitProposal(revision.getId(), student.getId());
        ReviewerAssignment revisionAssignment =
                proposalWorkflowFacade.assignReviewer(revision.getId(), faculty.getId(), admin.getId());
        proposalWorkflowFacade.submitReview(
                revision.getId(),
                faculty.getId(),
                revisionAssignment.getId(),
                "Clarify equipment categories and provide a conflict-resolution rule set before approval.",
                RecommendationType.REVISION);
        proposalWorkflowFacade.requestRevision(
                revision.getId(),
                admin.getId(),
                "Please address reviewer comments on equipment categories and conflict rules.");

        // 5) Approved proposal with history
        ResearchProposal approved = proposalWorkflowFacade.createDraft(student.getId(), content(
                "Approved: Alumni Mentorship Matching",
                "This proposal designs a mentorship matching process between alumni volunteers and students.",
                "Increase access to career guidance through structured mentor-student pairing.",
                "Pilot a matching workflow based on skills, availability and student goals.",
                "Expect successful mentor matches and positive qualitative feedback from participants."));
        approved = proposalWorkflowFacade.submitProposal(approved.getId(), student.getId());
        ReviewerAssignment approvedAssignment =
                proposalWorkflowFacade.assignReviewer(approved.getId(), faculty.getId(), admin.getId());
        proposalWorkflowFacade.submitReview(
                approved.getId(),
                faculty.getId(),
                approvedAssignment.getId(),
                "Well-scoped pilot with clear outcomes. Recommend approval for departmental support.",
                RecommendationType.APPROVE);
        proposalWorkflowFacade.approveProposal(
                approved.getId(),
                admin.getId(),
                "Approved for pilot implementation in the upcoming term.");

        // Sanity: ensure mixed statuses exist
        long distinctStatuses = researchProposalRepository.findByAuthorId(student.getId()).stream()
                .map(ResearchProposal::getStatus)
                .distinct()
                .count();
        if (distinctStatuses < 4) {
            throw new IllegalStateException("Demo seed did not create enough distinct proposal statuses");
        }
    }

    private ProposalContentCommand content(String title, String abstractText, String objectives,
                                           String methodology, String outcomes) {
        return new ProposalContentCommand(title, abstractText, objectives, methodology, outcomes);
    }

    private String optional(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
