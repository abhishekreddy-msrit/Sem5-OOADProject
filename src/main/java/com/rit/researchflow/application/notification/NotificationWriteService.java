package com.rit.researchflow.application.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.domain.notification.Notification;
import com.rit.researchflow.domain.notification.NotificationType;
import com.rit.researchflow.domain.proposal.ResearchProposal;
import com.rit.researchflow.domain.repository.NotificationRepository;
import com.rit.researchflow.domain.repository.ResearchProposalRepository;
import com.rit.researchflow.domain.repository.UserRepository;
import com.rit.researchflow.domain.user.User;

/**
 * Persists notifications in an independent transaction after the workflow commits.
 */
@Service
public class NotificationWriteService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ResearchProposalRepository researchProposalRepository;

    public NotificationWriteService(NotificationRepository notificationRepository,
                                    UserRepository userRepository,
                                    ResearchProposalRepository researchProposalRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.researchProposalRepository = researchProposalRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotification(Long recipientId, Long proposalId, NotificationType type, String message) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalStateException("Notification recipient not found: " + recipientId));
        ResearchProposal proposal = null;
        if (proposalId != null) {
            proposal = researchProposalRepository.findById(proposalId).orElse(null);
        }
        notificationRepository.save(new Notification(recipient, type, message, proposal));
    }
}
