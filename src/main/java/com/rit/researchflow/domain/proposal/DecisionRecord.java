package com.rit.researchflow.domain.proposal;

import java.time.Instant;
import java.util.Objects;

import com.rit.researchflow.domain.user.Administrator;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "decision_records")
public class DecisionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    private ResearchProposal proposal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decided_by_id", nullable = false)
    private Administrator decidedBy;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false, length = 40)
    private DecisionType decisionType;

    @Column(length = 2000)
    private String note;

    @Column(name = "override_of_recommendations", nullable = false)
    private boolean overrideOfRecommendations;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    protected DecisionRecord() {
        // JPA
    }

    DecisionRecord(ResearchProposal proposal, Administrator decidedBy, int revisionNumber,
                   DecisionType decisionType, String note, boolean overrideOfRecommendations, Instant decidedAt) {
        this.proposal = Objects.requireNonNull(proposal, "proposal");
        this.decidedBy = Objects.requireNonNull(decidedBy, "decidedBy");
        this.revisionNumber = revisionNumber;
        this.decisionType = Objects.requireNonNull(decisionType, "decisionType");
        this.note = note;
        this.overrideOfRecommendations = overrideOfRecommendations;
        this.decidedAt = Objects.requireNonNull(decidedAt, "decidedAt");
    }

    public Long getId() {
        return id;
    }

    public ResearchProposal getProposal() {
        return proposal;
    }

    public Administrator getDecidedBy() {
        return decidedBy;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public String getNote() {
        return note;
    }

    public boolean isOverrideOfRecommendations() {
        return overrideOfRecommendations;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
