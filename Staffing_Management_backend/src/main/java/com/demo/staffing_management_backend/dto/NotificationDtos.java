package com.demo.staffing_management_backend.dto;

import java.time.Instant;

public final class NotificationDtos {
    private NotificationDtos() {}

    public record NotificationRequest(
            String recipientId,
            String title,
            String message,
            String type) {
    }

    public record NotificationResponse(
            String id,
            String recipientId,
            String title,
            String message,
            String type,
            boolean read,
            Instant createdAt) {
    }
}
