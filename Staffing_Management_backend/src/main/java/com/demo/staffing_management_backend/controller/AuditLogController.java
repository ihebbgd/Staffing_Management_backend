package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.AuditLogDtos;
import com.demo.staffing_management_backend.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Audit Logs", description = "Security-relevant action history (admin only)")
public class AuditLogController {
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List audit log entries, newest first")
    public ResponseEntity<Page<AuditLogDtos.AuditLogResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(auditService.getAll(pageable));
    }
}
