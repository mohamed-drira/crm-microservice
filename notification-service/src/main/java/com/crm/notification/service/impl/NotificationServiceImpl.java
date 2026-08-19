package com.crm.notification.service.impl;

import com.crm.notification.dto.response.NotificationResponse;
import com.crm.notification.dto.response.UnreadCountResponse;
import com.crm.notification.entity.Notification;
import com.crm.notification.exception.ApiException;
import com.crm.notification.repository.NotificationRepository;
import com.crm.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByUser(String username) {
        return notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String username) {
        long count = notificationRepository.countByRecipientUsernameAndReadFalse(username);
        return UnreadCountResponse.builder().count(count).build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id, String username) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found: " + id));
        if (!notification.getRecipientUsername().equals(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to this notification");
        }
        notification.setRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(String username) {
        List<Notification> unread = notificationRepository
                .findByRecipientUsernameOrderByCreatedAtDesc(username).stream()
                .filter(n -> !n.isRead())
                .toList();
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
