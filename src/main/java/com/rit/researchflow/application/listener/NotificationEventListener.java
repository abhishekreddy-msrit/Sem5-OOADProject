package com.rit.researchflow.application.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rit.researchflow.application.event.ProposalWorkflowEvent;
import com.rit.researchflow.application.notification.NotificationWriteService;
import com.rit.researchflow.domain.notification.NotificationType;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.Administrator;

/**
 * Observer: reacts to committed workflow events by creating in-app notifications.
 */
@Component
public class NotificationEventListener {

    private final NotificationWriteService notificationWriteService;
    private final UserRepository userRepository;

    public NotificationEventListener(NotificationWriteService notificationWriteService,
                                     UserRepository userRepository) {
        this.notificationWriteService = notificationWriteService;
        this.userRepository = userRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalWorkflowEvent(ProposalWorkflowEvent event) {
        NotificationType type = event.getNotificationType();

        if (type == NotificationType.PROPOSAL_SUBMITTED || type == NotificationType.PROPOSAL_RESUBMITTED) {
            notifyAdministrators(event);
            return;
        }

        if (type == NotificationType.REVIEWER_ASSIGNED && event.getFacultyId() != null) {
            notificationWriteService.createNotification(
                    event.getFacultyId(), event.getProposalId(), type, event.getMessage());
            return;
        }

        if (type == NotificationType.REVIEW_SUBMITTED) {
            notifyAdministrators(event);
            if (event.getStudentId() != null) {
                notificationWriteService.createNotification(
                        event.getStudentId(), event.getProposalId(), type, event.getMessage());
            }
            return;
        }

        if ((type == NotificationType.REVISION_REQUIRED
                || type == NotificationType.PROPOSAL_APPROVED
                || type == NotificationType.PROPOSAL_REJECTED)
                && event.getStudentId() != null) {
            notificationWriteService.createNotification(
                    event.getStudentId(), event.getProposalId(), type, event.getMessage());
        }
    }

    private void notifyAdministrators(ProposalWorkflowEvent event) {
        for (Administrator admin : userRepository.findActiveAdministrators()) {
            notificationWriteService.createNotification(
                    admin.getId(), event.getProposalId(), event.getNotificationType(), event.getMessage());
        }
    }
}
