package com.rit.researchflow.domain.proposal;

import java.time.Instant;
import java.util.Objects;

import com.rit.researchflow.domain.user.Administrator;
import com.rit.researchflow.domain.user.Faculty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "reviewer_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assignment_proposal_faculty_revision",
                columnNames = {"proposal_id", "faculty_id", "revision_number"}
        )
)
public class ReviewerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    private ResearchProposal proposal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private Administrator assignedBy;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(nullable = false)
    private boolean active;

    @OneToOne(mappedBy = "assignment", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Review review;

    protected ReviewerAssignment() {
        // JPA
    }

    ReviewerAssignment(ResearchProposal proposal, Faculty faculty, Administrator assignedBy,
                       int revisionNumber, Instant assignedAt) {
        this.proposal = Objects.requireNonNull(proposal, "proposal");
        this.faculty = Objects.requireNonNull(faculty, "faculty");
        this.assignedBy = Objects.requireNonNull(assignedBy, "assignedBy");
        this.revisionNumber = revisionNumber;
        this.assignedAt = Objects.requireNonNull(assignedAt, "assignedAt");
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public ResearchProposal getProposal() {
        return proposal;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public Administrator getAssignedBy() {
        return assignedBy;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public boolean isActive() {
        return active;
    }

    public Review getReview() {
        return review;
    }

    public boolean belongsTo(Faculty candidate) {
        return faculty != null && faculty.equals(candidate);
    }

    public boolean matchesCycle(int cycle) {
        return revisionNumber == cycle;
    }

    void deactivate() {
        this.active = false;
    }

    void attachReview(Review review) {
        this.review = review;
    }
}
