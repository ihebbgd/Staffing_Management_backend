package com.demo.staffing_management_backend.dto;

import java.time.Instant;

public final class AuditLogDtos {

    private AuditLogDtos() {
    }

    public record AuditLogResponse(String id,
                                   String username,
                                   String action,
                                   String targetType,
                                   String targetId,
                                   String targetName,
                                   Instant timestamp,
                                   String details) {
    }
}
