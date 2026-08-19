package com.crm.notification.dto.response;

import com.crm.notification.entity.Notification;
import com.crm.notification.entity.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
