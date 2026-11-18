package com.rit.researchflow.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.domain.notification.Notification;
import com.rit.researchflow.domain.notification.NotificationType;
import com.rit.researchflow.domain.proposal.ProposalStatus;
import com.rit.researchflow.domain.proposal.RecommendationType;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.proposal.Review;
import com.rit.researchflow.domain.proposal.ReviewerAssignment;
import com.rit.researchflow.domain.repository.NotificationRepository;
import com.rit.researchflow.domain.repository.ResearchProposalRepository;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.Student;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DomainPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResearchProposalRepository researchProposalRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private Student student;
    private Faculty faculty;
    private Administrator admin;

    @BeforeEach
    void setUp() {
        assertTrue(datasourceUrl.contains("researchflow_test"),
                "Persistence tests must use researchflow_test, got: " + datasourceUrl);

        student = (Student) userRepository.save(new Student(
                "it_student_" + System.nanoTime(), "hash", "IT Student", "it.student@example.com", "CSE", "USN001"));
        faculty = (Faculty) userRepository.save(new Faculty(
                "it_faculty_" + System.nanoTime(), "hash", "IT Faculty", "it.faculty@example.com", "CSE", "Professor"));
        admin = (Administrator) userRepository.save(new Administrator(
                "it_admin_" + System.nanoTime(), "hash", "IT Admin", "it.admin@example.com", "Office"));
    }

    @Test
    void joinedUserHierarchyRoundTrip() {
        assertEquals("STUDENT", userRepository.findById(student.getId()).orElseThrow().getRoleName());
        assertEquals("FACULTY", userRepository.findById(faculty.getId()).orElseThrow().getRoleName());
        assertEquals("ADMINISTRATOR", userRepository.findById(admin.getId()).orElseThrow().getRoleName());
    }

    @Test
    void persistsProposalAssignmentAndReviewThroughAggregate() {
        ResearchProposal proposal = new ResearchProposal(
                student,
                "Edge AI Campus Energy Monitoring",
                "This proposal investigates edge-based monitoring for campus energy systems with practical pilots.",
                "Define measurable energy-saving objectives for campus buildings.",
                "Use edge devices with a controlled pilot deployment methodology.",
                "Expect reduced energy waste and actionable operational insights.");
        proposal.submit();
        ReviewerAssignment assignment = proposal.assignReviewer(faculty, admin);
        proposal.addReview(assignment, "Solid methodology with clear objectives for the pilot.", RecommendationType.APPROVE);

        ResearchProposal saved = researchProposalRepository.save(proposal);
        entityManager.flush();
        entityManager.clear();

        ResearchProposal loaded = researchProposalRepository.findById(saved.getId()).orElseThrow();
        assertEquals(ProposalStatus.UNDER_REVIEW, loaded.getStatus());
        assertEquals(1, loaded.getRevisionNumber());
        assertEquals(1, loaded.getAssignments().size());

        ReviewerAssignment loadedAssignment = loaded.getAssignments().get(0);
        Review loadedReview = loadedAssignment.getReview();
        assertNotNull(loadedReview);
        assertEquals(RecommendationType.APPROVE, loadedReview.getRecommendation());
        assertEquals(loaded.getId(), loadedReview.getProposal().getId());
        assertEquals(faculty.getId(), loadedReview.getFaculty().getId());
        assertEquals(1, loaded.getReviewsForCurrentCycle().size());
    }

    @Test
    void databaseEnforcesOneReviewPerAssignment() {
        ResearchProposal proposal = researchProposalRepository.save(submittedUnderReview());
        entityManager.flush();

        ReviewerAssignment assignment = proposal.getActiveAssignments().get(0);

        // Aggregate already persisted one review for this assignment; a second row must violate UNIQUE(assignment_id)
        // Native EntityManager updates surface Hibernate ConstraintViolationException (not Spring's DataIntegrityViolationException)
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.createNativeQuery(
                            "INSERT INTO reviews (assignment_id, revision_number, comments, recommendation, submitted_at) "
                                    + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP(6))")
                    .setParameter(1, assignment.getId())
                    .setParameter(2, 1)
                    .setParameter(3, "Duplicate review row for uniqueness verification.")
                    .setParameter(4, "REJECT")
                    .executeUpdate();
            entityManager.flush();
        });
    }

    @Test
    void databaseEnforcesUniqueFacultyAssignmentPerRevision() {
        ResearchProposal proposal = researchProposalRepository.save(submittedUnderReview());
        entityManager.flush();

        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.createNativeQuery(
                            "INSERT INTO reviewer_assignments "
                                    + "(proposal_id, faculty_id, assigned_by_id, revision_number, assigned_at, active) "
                                    + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP(6), TRUE)")
                    .setParameter(1, proposal.getId())
                    .setParameter(2, faculty.getId())
                    .setParameter(3, admin.getId())
                    .setParameter(4, 1)
                    .executeUpdate();
            entityManager.flush();
        });
    }

    @Test
    void persistsNotificationWithoutObserverWorkflow() {
        ResearchProposal proposal = researchProposalRepository.save(newDraft());
        Notification notification = notificationRepository.save(new Notification(
                student,
                NotificationType.PROPOSAL_SUBMITTED,
                "Your proposal was recorded in the system.",
                proposal));

        entityManager.flush();
        entityManager.clear();

        Notification loaded = notificationRepository.findById(notification.getId()).orElseThrow();
        assertEquals(NotificationType.PROPOSAL_SUBMITTED, loaded.getType());
        assertEquals(student.getId(), loaded.getRecipient().getId());
        assertFalseReadThenMark(loaded);
    }

    private void assertFalseReadThenMark(Notification loaded) {
        assertEquals(false, loaded.isRead());
        loaded.markRead();
        notificationRepository.save(loaded);
        entityManager.flush();
        assertTrue(notificationRepository.findById(loaded.getId()).orElseThrow().isRead());
    }

    private ResearchProposal newDraft() {
        return new ResearchProposal(
                student,
                "Edge AI Campus Energy Monitoring",
                "This proposal investigates edge-based monitoring for campus energy systems with practical pilots.",
                "Define measurable energy-saving objectives for campus buildings.",
                "Use edge devices with a controlled pilot deployment methodology.",
                "Expect reduced energy waste and actionable operational insights.");
    }

    private ResearchProposal submittedUnderReview() {
        ResearchProposal proposal = newDraft();
        proposal.submit();
        proposal.assignReviewer(faculty, admin);
        proposal.addReview(
                proposal.getActiveAssignments().get(0),
                "Solid methodology with clear objectives for the pilot.",
                RecommendationType.APPROVE);
        return proposal;
    }
}

