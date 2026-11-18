package com.rit.researchflow.application.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rit.researchflow.application.exception.ApplicationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.application.dto.ProposalContentCommand;
import com.rit.researchflow.application.dto.ReviewProgressView;
import com.rit.researchflow.domain.proposal.ProposalStatus;
import com.rit.researchflow.domain.proposal.RecommendationType;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.proposal.ReviewerAssignment;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.Student;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProposalWorkflowFacadeTest {

    @Autowired
    private ProposalWorkflowFacade facade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student student;
    private Faculty faculty;
    private Administrator admin;

    @BeforeEach
    void setUp() {
        long n = System.nanoTime();
        student = (Student) userRepository.save(new Student(
                "facade_s_" + n, passwordEncoder.encode("password123"), "Facade Student",
                "facade.s@example.com", "CSE", "USN-F"));
        faculty = (Faculty) userRepository.save(new Faculty(
                "facade_f_" + n, passwordEncoder.encode("password123"), "Facade Faculty",
                "facade.f@example.com", "CSE", "Professor"));
        admin = (Administrator) userRepository.save(new Administrator(
                "facade_a_" + n, passwordEncoder.encode("password123"), "Facade Admin",
                "facade.a@example.com", "Office"));
    }

    @Test
    void submitAssignReviewAndApproveThroughFacade() {
        ResearchProposal draft = facade.createDraft(student.getId(), sampleContent("Facade Approval Path Study"));
        assertEquals(ProposalStatus.DRAFT, draft.getStatus());

        ResearchProposal submitted = facade.submitProposal(draft.getId(), student.getId());
        assertEquals(ProposalStatus.SUBMITTED, submitted.getStatus());
        assertEquals(1, submitted.getRevisionNumber());

        ReviewerAssignment assignment = facade.assignReviewer(submitted.getId(), faculty.getId(), admin.getId());
        ReviewProgressView progress = facade.getReviewProgress(submitted.getId());
        assertEquals(ProposalStatus.UNDER_REVIEW, progress.status());
        assertEquals(1, progress.reviewers().size());
        assertFalse(progress.reviewers().get(0).reviewSubmitted());

        facade.submitReview(
                submitted.getId(),
                faculty.getId(),
                assignment.getId(),
                "Clear objectives and a feasible methodology for the pilot study.",
                RecommendationType.APPROVE);

        progress = facade.getReviewProgress(submitted.getId());
        assertTrue(progress.reviewers().get(0).reviewSubmitted());

        facade.approveProposal(submitted.getId(), admin.getId(), "Approved for demonstration.");
        assertEquals(ProposalStatus.APPROVED, facade.getProposal(submitted.getId()).getStatus());
    }

    @Test
    void facultyDetailAccessRequiresActiveAssignmentForCurrentRevisionCycle() {
        long n = System.nanoTime();
        Faculty otherFaculty = (Faculty) userRepository.save(new Faculty(
                "facade_f2_" + n, passwordEncoder.encode("password123"), "Other Faculty",
                "facade.f2@example.com", "CSE", "Assistant Professor"));

        ResearchProposal draft = facade.createDraft(student.getId(), sampleContent("Active Cycle Access Study"));
        ResearchProposal submitted = facade.submitProposal(draft.getId(), student.getId());
        ReviewerAssignment assignment =
                facade.assignReviewer(submitted.getId(), faculty.getId(), admin.getId());

        // Active current-cycle assignee can open the proposal.
        ResearchProposal visible = facade.getAssignedProposal(submitted.getId(), faculty.getId());
        assertEquals(submitted.getId(), visible.getId());

        // Unassigned faculty cannot open it.
        assertThrows(ApplicationException.class,
                () -> facade.getAssignedProposal(submitted.getId(), otherFaculty.getId()));

        facade.submitReview(
                submitted.getId(),
                faculty.getId(),
                assignment.getId(),
                "Needs clearer measurable outcomes before another review cycle can proceed.",
                RecommendationType.REVISION);
        facade.requestRevision(
                submitted.getId(),
                admin.getId(),
                "Please revise objectives and resubmit for a new review cycle.");

        // Still active on revision-required: access remains until resubmission deactivates the cycle.
        facade.getAssignedProposal(submitted.getId(), faculty.getId());

        ResearchProposal editable = facade.getOwnedProposal(submitted.getId(), student.getId());
        facade.updateContent(editable.getId(), student.getId(), sampleContent("Active Cycle Access Study Revised"));
        ResearchProposal resubmitted = facade.submitProposal(editable.getId(), student.getId());
        assertEquals(2, resubmitted.getRevisionNumber());
        assertTrue(resubmitted.getAssignments().stream()
                .filter(a -> a.getRevisionNumber() == 1)
                .allMatch(a -> !a.isActive()));

        // Historical cycle-1 assignment no longer grants detail access.
        assertThrows(ApplicationException.class,
                () -> facade.getAssignedProposal(resubmitted.getId(), faculty.getId()));
    }

    private ProposalContentCommand sampleContent(String title) {
        return new ProposalContentCommand(
                title,
                "This facade test proposal investigates structured academic review workflows in a controlled setting.",
                "Demonstrate end-to-end proposal submission, review and institutional decision handling.",
                "Use the ResearchFlow facade and domain aggregate to drive the lifecycle under test.",
                "Expect a coherent approval path that preserves assignment and review history.");
    }
}
