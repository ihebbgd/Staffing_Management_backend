package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.service.BenchService;
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
@RequestMapping("/api/bench")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bench", description = "Active employees with no current allocations")
public class BenchController {
    private final BenchService benchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List bench employees (active employees with no active allocations)")
    public ResponseEntity<List<EmployeeDtos.EmployeeResponse>> getBench() {
        return ResponseEntity.ok(benchService.getBench());
    }
}
