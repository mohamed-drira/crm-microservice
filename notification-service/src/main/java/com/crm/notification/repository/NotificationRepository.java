package com.crm.notification.repository;

import com.crm.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    List<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    long countByRecipientUsernameAndReadFalse(String recipientUsername);
}
