package com.rit.researchflow.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.rit.researchflow.domain.notification.Notification;
import com.rit.researchflow.domain.repository.NotificationRepository;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(SpringDataNotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId) {
        return jpaRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }
}
