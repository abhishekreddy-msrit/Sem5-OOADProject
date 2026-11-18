package com.rit.researchflow.domain.proposal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.rit.researchflow.domain.exception.DomainValidationException;
import com.rit.researchflow.domain.exception.IllegalProposalTransitionException;
import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;
import com.rit.researchflow.domain.user.Student;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Aggregate root for proposal content, lifecycle, assignments and decisions.
 * Reviews are reached only through {@link ReviewerAssignment}.
 */
@Entity
@Table(name = "research_proposals")
public class ResearchProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Student author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "abstract_text", nullable = false, length = 4000)
    private String abstractText;

    @Column(nullable = false, length = 4000)
    private String objectives;

    @Column(nullable = false, length = 4000)
    private String methodology;

    @Column(name = "expected_outcomes", nullable = false, length = 4000)
    private String expectedOutcomes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProposalStatus status;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @OneToMany(mappedBy = "proposal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<ReviewerAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "proposal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<DecisionRecord> decisions = new ArrayList<>();

    protected ResearchProposal() {
        // JPA
    }

    public ResearchProposal(Student author, String title, String abstractText, String objectives,
                            String methodology, String expectedOutcomes) {
        this.author = Objects.requireNonNull(author, "author");
        applyContent(title, abstractText, objectives, methodology, expectedOutcomes);
        this.status = ProposalStatus.DRAFT;
        this.revisionNumber = 0;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Student getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String getObjectives() {
        return objectives;
    }

    public String getMethodology() {
        return methodology;
    }

    public String getExpectedOutcomes() {
        return expectedOutcomes;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public List<ReviewerAssignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public List<DecisionRecord> getDecisions() {
        return Collections.unmodifiableList(decisions);
    }

    public boolean isEditable() {
        return status == ProposalStatus.DRAFT || status == ProposalStatus.REVISION_REQUIRED;
    }

    public List<ReviewerAssignment> getActiveAssignments() {
        return assignments.stream().filter(ReviewerAssignment::isActive).collect(Collectors.toList());
    }

    public List<Review> getReviewsForCurrentCycle() {
        return assignments.stream()
                .filter(a -> a.matchesCycle(revisionNumber))
                .map(ReviewerAssignment::getReview)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Faculty> getPendingReviewers() {
        return assignments.stream()
                .filter(ReviewerAssignment::isActive)
                .filter(a -> a.matchesCycle(revisionNumber))
                .filter(a -> a.getReview() == null)
                .map(ReviewerAssignment::getFaculty)
                .collect(Collectors.toList());
    }

    public List<RecommendationType> getCurrentCycleRecommendations() {
        return getReviewsForCurrentCycle().stream()
                .map(Review::getRecommendation)
                .collect(Collectors.toList());
    }

    public void updateContent(String title, String abstractText, String objectives,
                              String methodology, String expectedOutcomes) {
        if (!isEditable()) {
            throw new IllegalProposalTransitionException(status, "update content");
        }
        applyContent(title, abstractText, objectives, methodology, expectedOutcomes);
        touch();
    }

    public void submit() {
        if (status != ProposalStatus.DRAFT && status != ProposalStatus.REVISION_REQUIRED) {
            throw new IllegalProposalTransitionException(status, "submit");
        }
        assertContentComplete();

        if (status == ProposalStatus.DRAFT) {
            this.revisionNumber = 1;
        } else {
            deactivateAssignmentsForCycle(this.revisionNumber);
            this.revisionNumber = this.revisionNumber + 1;
        }

        this.status = ProposalStatus.SUBMITTED;
        this.submittedAt = Instant.now();
        touch();
    }

    public ReviewerAssignment assignReviewer(Faculty faculty, Administrator assignedBy) {
        Objects.requireNonNull(faculty, "faculty");
        Objects.requireNonNull(assignedBy, "assignedBy");

        if (status != ProposalStatus.SUBMITTED && status != ProposalStatus.UNDER_REVIEW) {
            throw new IllegalProposalTransitionException(status, "assign reviewer");
        }
        if (status == ProposalStatus.UNDER_REVIEW && revisionNumber < 1) {
            throw new DomainValidationException("Cannot assign reviewers before first submission");
        }

        boolean duplicate = assignments.stream().anyMatch(a ->
                a.matchesCycle(revisionNumber) && a.belongsTo(faculty));
        if (duplicate) {
            throw new DomainValidationException(
                    "Faculty is already assigned to this proposal for the current revision cycle");
        }

        ReviewerAssignment assignment = new ReviewerAssignment(this, faculty, assignedBy, revisionNumber, Instant.now());
        assignments.add(assignment);

        if (status == ProposalStatus.SUBMITTED) {
            this.status = ProposalStatus.UNDER_REVIEW;
        }
        touch();
        return assignment;
    }

    public Review addReview(ReviewerAssignment assignment, String comments, RecommendationType recommendation) {
        Objects.requireNonNull(assignment, "assignment");
        Objects.requireNonNull(recommendation, "recommendation");

        if (status != ProposalStatus.UNDER_REVIEW) {
            throw new IllegalProposalTransitionException(status, "add review");
        }
        if (assignment.getProposal() != this) {
            throw new DomainValidationException("Assignment does not belong to this proposal");
        }
        if (!assignment.isActive() || !assignment.matchesCycle(revisionNumber)) {
            throw new DomainValidationException("Assignment is not active for the current revision cycle");
        }
        if (assignment.getReview() != null) {
            throw new DomainValidationException("A review already exists for this assignment");
        }

        String normalizedComments = requireText(comments, "Comments", 10, 4000);
        Review review = new Review(assignment, normalizedComments, recommendation, Instant.now());
        assignment.attachReview(review);
        touch();
        return review;
    }

    public DecisionRecord requestRevision(Administrator admin, String note) {
        Objects.requireNonNull(admin, "admin");
        requireUnderReviewWithAtLeastOneReview();
        String normalizedNote = requireMandatoryNote(note, "Revision request");

        DecisionRecord record = new DecisionRecord(
                this, admin, revisionNumber, DecisionType.REQUEST_REVISION, normalizedNote, false, Instant.now());
        decisions.add(record);
        this.status = ProposalStatus.REVISION_REQUIRED;
        touch();
        return record;
    }

    public DecisionRecord approve(Administrator admin, String note) {
        Objects.requireNonNull(admin, "admin");
        requireUnderReviewWithAtLeastOneReview();

        boolean override = contradictsAllRecommendations(DecisionType.APPROVE);
        String normalizedNote = resolveDecisionNote(note, override, false, "Approval");

        DecisionRecord record = new DecisionRecord(
                this, admin, revisionNumber, DecisionType.APPROVE, normalizedNote, override, Instant.now());
        decisions.add(record);
        this.status = ProposalStatus.APPROVED;
        touch();
        return record;
    }

    public DecisionRecord reject(Administrator admin, String note) {
        Objects.requireNonNull(admin, "admin");
        requireUnderReviewWithAtLeastOneReview();

        boolean override = contradictsAllRecommendations(DecisionType.REJECT);
        String normalizedNote = resolveDecisionNote(note, override, true, "Rejection");

        DecisionRecord record = new DecisionRecord(
                this, admin, revisionNumber, DecisionType.REJECT, normalizedNote, override, Instant.now());
        decisions.add(record);
        this.status = ProposalStatus.REJECTED;
        touch();
        return record;
    }

    private void requireUnderReviewWithAtLeastOneReview() {
        if (status != ProposalStatus.UNDER_REVIEW) {
            throw new IllegalProposalTransitionException(status, "record decision");
        }
        if (getReviewsForCurrentCycle().isEmpty()) {
            throw new DomainValidationException("At least one faculty review is required in the current cycle");
        }
    }

    private boolean contradictsAllRecommendations(DecisionType decision) {
        List<RecommendationType> recommendations = getCurrentCycleRecommendations();
        if (recommendations.isEmpty()) {
            return false;
        }
        RecommendationType matching = mapDecisionToRecommendation(decision);
        return recommendations.stream().noneMatch(r -> r == matching);
    }

    private RecommendationType mapDecisionToRecommendation(DecisionType decision) {
        return switch (decision) {
            case APPROVE -> RecommendationType.APPROVE;
            case REJECT -> RecommendationType.REJECT;
            case REQUEST_REVISION -> RecommendationType.REVISION;
        };
    }

    private String resolveDecisionNote(String note, boolean override, boolean alwaysMandatory, String label) {
        if (alwaysMandatory || override) {
            return requireMandatoryNote(note, label);
        }
        if (note == null || note.isBlank()) {
            return null;
        }
        return requireText(note, label + " note", 1, 2000);
    }

    private String requireMandatoryNote(String note, String label) {
        if (note == null || note.isBlank()) {
            throw new DomainValidationException(label + " note is mandatory");
        }
        return requireText(note, label + " note", 1, 2000);
    }

    private void deactivateAssignmentsForCycle(int cycle) {
        assignments.stream()
                .filter(a -> a.matchesCycle(cycle) && a.isActive())
                .forEach(ReviewerAssignment::deactivate);
    }

    private void applyContent(String title, String abstractText, String objectives,
                              String methodology, String expectedOutcomes) {
        this.title = requireText(title, "Title", 5, 200);
        this.abstractText = requireText(abstractText, "Abstract", 50, 4000);
        this.objectives = requireText(objectives, "Research objectives", 20, 4000);
        this.methodology = requireText(methodology, "Methodology", 20, 4000);
        this.expectedOutcomes = requireText(expectedOutcomes, "Expected outcomes", 20, 4000);
    }

    private void assertContentComplete() {
        applyContent(title, abstractText, objectives, methodology, expectedOutcomes);
    }

    private String requireText(String value, String label, int min, int max) {
        if (value == null) {
            throw new DomainValidationException(label + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new DomainValidationException(label + " must be between " + min + " and " + max + " characters");
        }
        return trimmed;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
