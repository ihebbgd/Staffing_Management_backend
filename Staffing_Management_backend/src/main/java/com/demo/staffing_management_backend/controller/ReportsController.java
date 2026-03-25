package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.ReportDtos;
import com.demo.staffing_management_backend.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "Aggregation reports (JSON only)")
public class ReportsController {
    private final ReportsService reportsService;

    @GetMapping("/utilization")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Per-employee utilization summary with status buckets")
    public ResponseEntity<List<ReportDtos.UtilizationReportRow>> utilization() {
        return ResponseEntity.ok(reportsService.utilizationReport());
    }

    @GetMapping("/certifications")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Certification counts and lists grouped by status")
    public ResponseEntity<ReportDtos.CertificationReport> certifications() {
        return ResponseEntity.ok(reportsService.certificationReport());
    }
}
