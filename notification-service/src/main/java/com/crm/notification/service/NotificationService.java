package com.crm.notification.service;

import com.crm.notification.dto.response.NotificationResponse;
import com.crm.notification.dto.response.UnreadCountResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getByUser(String username);

    UnreadCountResponse getUnreadCount(String username);

    NotificationResponse markAsRead(Long id, String username);

    void markAllAsRead(String username);
}
