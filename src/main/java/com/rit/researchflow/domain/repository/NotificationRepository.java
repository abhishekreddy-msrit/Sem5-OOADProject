package com.rit.researchflow.domain.repository;

import java.util.List;
import java.util.Optional;

import com.rit.researchflow.domain.notification.Notification;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
