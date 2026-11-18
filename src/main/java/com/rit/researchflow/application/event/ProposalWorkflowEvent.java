package com.rit.researchflow.application.event;

import com.rit.researchflow.domain.notification.NotificationType;

/**
 * Application event published after successful proposal workflow mutations.
 */
public class ProposalWorkflowEvent {

    private final Long proposalId;
    private final Long studentId;
    private final Long facultyId;
    private final NotificationType notificationType;
    private final String message;

    public ProposalWorkflowEvent(Long proposalId, Long studentId, Long facultyId,
                                 NotificationType notificationType, String message) {
        this.proposalId = proposalId;
        this.studentId = studentId;
        this.facultyId = facultyId;
        this.notificationType = notificationType;
        this.message = message;
    }

    public Long getProposalId() {
        return proposalId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getMessage() {
        return message;
    }
}
