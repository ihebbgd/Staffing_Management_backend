package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.DashboardDtos;
import com.demo.staffing_management_backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Aggregate staffing statistics for the overview dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Aggregate staffing statistics (counts, utilization, certification expiry buckets)")
    public ResponseEntity<DashboardDtos.StatsResponse> stats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
