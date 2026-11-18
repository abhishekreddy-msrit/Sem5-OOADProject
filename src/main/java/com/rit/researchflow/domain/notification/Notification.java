package com.rit.researchflow.domain.notification;

import java.time.Instant;
import java.util.Objects;

import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.user.User;

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
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id")
    private ResearchProposal proposal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "read_flag", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
        // JPA
    }

    public Notification(User recipient, NotificationType type, String message, ResearchProposal proposal) {
        this.recipient = Objects.requireNonNull(recipient, "recipient");
        this.type = Objects.requireNonNull(type, "type");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
        String trimmed = message.trim();
        if (trimmed.length() > 1000) {
            throw new IllegalArgumentException("message must be at most 1000 characters");
        }
        this.message = trimmed;
        this.proposal = proposal;
        this.read = false;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public ResearchProposal getProposal() {
        return proposal;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markRead() {
        this.read = true;
    }
}
