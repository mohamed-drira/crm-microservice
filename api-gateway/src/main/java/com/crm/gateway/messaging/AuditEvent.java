package com.crm.gateway.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditEvent(
        UUID eventId,
        LocalDateTime timestamp,
        String method,
        String path,
        String userId,
        String username,
        String roles,
        int statusCode,
        long durationMs,
        String clientIp
) {
    public static AuditEvent of(String method, String path, String userId, String username,
                                String roles, int statusCode, long durationMs, String clientIp) {
        return new AuditEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                method,
                path,
                userId,
                username,
                roles,
                statusCode,
                durationMs,
                clientIp
        );
    }
}
