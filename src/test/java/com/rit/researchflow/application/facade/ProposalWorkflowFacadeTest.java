package com.rit.researchflow.application.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private ProposalContentCommand sampleContent(String title) {
        return new ProposalContentCommand(
                title,
                "This facade test proposal investigates structured academic review workflows in a controlled setting.",
                "Demonstrate end-to-end proposal submission, review and institutional decision handling.",
                "Use the ResearchFlow facade and domain aggregate to drive the lifecycle under test.",
                "Expect a coherent approval path that preserves assignment and review history.");
    }
}
