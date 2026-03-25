package com.demo.staffing_management_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "auditLogs")
public class AuditLog {
    @Id
    private String id;

    private String username;

    private String action;

    private String targetType;

    private String targetId;

    @Indexed
    private Instant timestamp;

    private String details;
}
