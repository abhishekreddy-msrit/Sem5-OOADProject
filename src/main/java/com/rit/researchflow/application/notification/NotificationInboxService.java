package com.rit.researchflow.application.notification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rit.researchflow.application.exception.ApplicationException;
import com.rit.researchflow.domain.notification.Notification;
import com.rit.researchflow.domain.repository.NotificationRepository;

/**
 * Application service for the logged-in user's notification inbox.
 */
@Service
public class NotificationInboxService {

    private final NotificationRepository notificationRepository;

    public NotificationInboxService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<Notification> listForUser(Long userId) {
        List<Notification> notifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        // open-in-view is disabled; initialize associations used by the inbox template
        for (Notification notification : notifications) {
            notification.getMessage();
            notification.getType();
            if (notification.getProposal() != null) {
                notification.getProposal().getTitle();
            }
        }
        return notifications;
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> !n.isRead())
                .count();
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApplicationException("Notification not found"));
        if (notification.getRecipient() == null || !notification.getRecipient().getId().equals(userId)) {
            throw new ApplicationException("You can only open your own notifications");
        }
        notification.markRead();
        notificationRepository.save(notification);
    }
}
