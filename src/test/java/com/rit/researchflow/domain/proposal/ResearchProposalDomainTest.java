package com.rit.researchflow.domain.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rit.researchflow.domain.exception.DomainValidationException;
import com.rit.researchflow.domain.exception.IllegalProposalTransitionException;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.Student;

class ResearchProposalDomainTest {

    private Student student;
    private Faculty faculty;
    private Administrator admin;

    @BeforeEach
    void setUp() {
        student = new Student("student1", "hash", "Student One", "s1@example.com", "CSE", "1MS21CS001");
        faculty = new Faculty("faculty1", "hash", "Faculty One", "f1@example.com", "CSE", "Professor");
        admin = new Administrator("admin", "hash", "Admin User", "admin@example.com", "Dean Office");
    }

    @Test
    void draftStartsAtRevisionZero() {
        ResearchProposal proposal = newProposal();
        assertEquals(ProposalStatus.DRAFT, proposal.getStatus());
        assertEquals(0, proposal.getRevisionNumber());
    }

    @Test
    void firstSubmitSetsRevisionToOne() {
        ResearchProposal proposal = newProposal();
        proposal.submit();
        assertEquals(ProposalStatus.SUBMITTED, proposal.getStatus());
        assertEquals(1, proposal.getRevisionNumber());
    }

    @Test
    void assignReviewerMovesToUnderReview() {
        ResearchProposal proposal = newProposal();
        proposal.submit();
        ReviewerAssignment assignment = proposal.assignReviewer(faculty, admin);

        assertEquals(ProposalStatus.UNDER_REVIEW, proposal.getStatus());
        assertTrue(assignment.isActive());
        assertEquals(1, assignment.getRevisionNumber());
    }

    @Test
    void rejectsDuplicateFacultyAssignmentInSameCycle() {
        ResearchProposal proposal = submittedUnderReview();
        assertThrows(DomainValidationException.class, () -> proposal.assignReviewer(faculty, admin));
    }

    @Test
    void addReviewIsReachableOnlyThroughAssignment() {
        ResearchProposal proposal = submittedUnderReview();
        ReviewerAssignment assignment = proposal.getActiveAssignments().get(0);

        Review review = proposal.addReview(assignment, "Solid methodology with clear objectives.", RecommendationType.APPROVE);

        assertEquals(1, proposal.getReviewsForCurrentCycle().size());
        assertEquals(review, assignment.getReview());
        assertEquals(faculty, review.getFaculty());
        assertEquals(proposal, review.getProposal());
        assertTrue(proposal.getPendingReviewers().isEmpty());
    }

    @Test
    void rejectsSecondReviewForSameAssignment() {
        ResearchProposal proposal = submittedUnderReview();
        ReviewerAssignment assignment = proposal.getActiveAssignments().get(0);
        proposal.addReview(assignment, "Solid methodology with clear objectives.", RecommendationType.APPROVE);

        assertThrows(DomainValidationException.class,
                () -> proposal.addReview(assignment, "Another review should not be allowed here.", RecommendationType.REJECT));
    }

    @Test
    void requestRevisionDoesNotIncrementRevisionNumber() {
        ResearchProposal proposal = reviewedProposal(RecommendationType.REVISION);
        proposal.requestRevision(admin, "Please clarify the evaluation metrics.");

        assertEquals(ProposalStatus.REVISION_REQUIRED, proposal.getStatus());
        assertEquals(1, proposal.getRevisionNumber());
    }

    @Test
    void resubmitIncrementsRevisionAndDeactivatesPriorAssignments() {
        ResearchProposal proposal = reviewedProposal(RecommendationType.REVISION);
        proposal.requestRevision(admin, "Please clarify the evaluation metrics.");
        ReviewerAssignment prior = proposal.getAssignments().get(0);

        proposal.updateContent(
                "Edge AI Campus Energy Monitoring",
                abstractText(),
                "Define measurable energy-saving objectives for campus buildings.",
                "Use edge devices with a controlled pilot deployment methodology.",
                "Expect reduced energy waste and actionable operational insights.");
        proposal.submit();

        assertEquals(ProposalStatus.SUBMITTED, proposal.getStatus());
        assertEquals(2, proposal.getRevisionNumber());
        assertFalse(prior.isActive());
        assertTrue(prior.getReview() != null);
    }

    @Test
    void approveRequiresNoteWhenOverridingAllRecommendations() {
        ResearchProposal proposal = reviewedProposal(RecommendationType.REJECT);
        assertThrows(DomainValidationException.class, () -> proposal.approve(admin, null));

        DecisionRecord decision = proposal.approve(admin, "Institutional priority overrides faculty recommendation.");
        assertEquals(ProposalStatus.APPROVED, proposal.getStatus());
        assertTrue(decision.isOverrideOfRecommendations());
    }

    @Test
    void rejectRequiresNote() {
        ResearchProposal proposal = reviewedProposal(RecommendationType.REJECT);
        assertThrows(DomainValidationException.class, () -> proposal.reject(admin, " "));
        proposal.reject(admin, "Scope is insufficient for departmental research standards.");
        assertEquals(ProposalStatus.REJECTED, proposal.getStatus());
    }

    @Test
    void cannotEditWhenUnderReview() {
        ResearchProposal proposal = submittedUnderReview();
        assertThrows(IllegalProposalTransitionException.class,
                () -> proposal.updateContent("x", abstractText(), objectives(), methodology(), outcomes()));
    }

    private ResearchProposal submittedUnderReview() {
        ResearchProposal proposal = newProposal();
        proposal.submit();
        proposal.assignReviewer(faculty, admin);
        return proposal;
    }

    private ResearchProposal reviewedProposal(RecommendationType recommendation) {
        ResearchProposal proposal = submittedUnderReview();
        ReviewerAssignment assignment = proposal.getActiveAssignments().get(0);
        proposal.addReview(assignment, "Detailed comments about strengths and weaknesses.", recommendation);
        return proposal;
    }

    private ResearchProposal newProposal() {
        return new ResearchProposal(student, "Edge AI Campus Energy Monitoring", abstractText(),
                objectives(), methodology(), outcomes());
    }

    private String abstractText() {
        return "This proposal investigates edge-based monitoring for campus energy systems with practical pilots.";
    }

    private String objectives() {
        return "Define measurable energy-saving objectives for campus buildings.";
    }

    private String methodology() {
        return "Use edge devices with a controlled pilot deployment methodology.";
    }

    private String outcomes() {
        return "Expect reduced energy waste and actionable operational insights.";
    }
}
