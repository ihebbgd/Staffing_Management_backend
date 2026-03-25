package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.AuditLogDtos;
import com.demo.staffing_management_backend.model.AuditLog;
import com.demo.staffing_management_backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    /** Records an audit entry attributed to the current principal (or "system"). Never log secrets in details. */
    public void record(String action, String targetType, String targetId, String details) {
        auditLogRepository.save(AuditLog.builder()
                .username(currentUsername())
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .timestamp(Instant.now())
                .build());
    }

    public Page<AuditLogDtos.AuditLogResponse> getAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable).map(this::toResponse);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            return "system";
        }
        return authentication.getName();
    }

    private AuditLogDtos.AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogDtos.AuditLogResponse(log.getId(), log.getUsername(), log.getAction(),
                log.getTargetType(), log.getTargetId(), log.getTimestamp(), log.getDetails());
    }
}
