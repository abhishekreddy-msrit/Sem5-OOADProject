package com.rit.researchflow.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rit.researchflow.domain.notification.Notification;

public interface SpringDataNotificationJpaRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
