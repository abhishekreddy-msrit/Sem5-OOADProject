package com.rit.researchflow.domain.proposal;

import java.time.Instant;
import java.util.Objects;

import com.rit.researchflow.domain.user.Faculty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Immutable faculty evaluation for one assignment in one revision cycle.
 * Faculty and proposal are reached only through {@link ReviewerAssignment}.
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, unique = true)
    private ReviewerAssignment assignment;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(nullable = false, length = 4000)
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RecommendationType recommendation;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    protected Review() {
        // JPA
    }

    Review(ReviewerAssignment assignment, String comments, RecommendationType recommendation, Instant submittedAt) {
        this.assignment = Objects.requireNonNull(assignment, "assignment");
        this.revisionNumber = assignment.getRevisionNumber();
        this.comments = comments;
        this.recommendation = Objects.requireNonNull(recommendation, "recommendation");
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
    }

    public Long getId() {
        return id;
    }

    public ReviewerAssignment getAssignment() {
        return assignment;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public String getComments() {
        return comments;
    }

    public RecommendationType getRecommendation() {
        return recommendation;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Faculty getFaculty() {
        return assignment.getFaculty();
    }

    public ResearchProposal getProposal() {
        return assignment.getProposal();
    }
}
