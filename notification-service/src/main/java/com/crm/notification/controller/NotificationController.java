package com.crm.notification.controller;

import com.crm.notification.dto.response.NotificationResponse;
import com.crm.notification.dto.response.UnreadCountResponse;
import com.crm.notification.security.CurrentUser;
import com.crm.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "User notifications from domain events")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List current user's notifications")
    public List<NotificationResponse> getByUser(@AuthenticationPrincipal CurrentUser user) {
        return notificationService.getByUser(user.getUsername());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Count unread notifications")
    public UnreadCountResponse getUnreadCount(@AuthenticationPrincipal CurrentUser user) {
        return notificationService.getUnreadCount(user.getUsername());
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public NotificationResponse markAsRead(@PathVariable Long id,
                                           @AuthenticationPrincipal CurrentUser user) {
        return notificationService.markAsRead(id, user.getUsername());
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark all notifications as read")
    public void markAllAsRead(@AuthenticationPrincipal CurrentUser user) {
        notificationService.markAllAsRead(user.getUsername());
    }
}
