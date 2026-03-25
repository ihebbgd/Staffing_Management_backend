package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.service.AllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Allocations", description = "Assign employees to projects and track workload")
public class AllocationController {
    private final AllocationService allocationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create an allocation (assign an employee to a project)")
    public ResponseEntity<AllocationDtos.AllocationResponse> create(@Valid @RequestBody AllocationDtos.AllocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(allocationService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List a page of allocations")
    public ResponseEntity<Page<AllocationDtos.AllocationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(allocationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one allocation by id")
    public ResponseEntity<AllocationDtos.AllocationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(allocationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List all allocations for a given employee (employees use /api/me/allocations)")
    public ResponseEntity<List<AllocationDtos.AllocationResponse>> getByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(allocationService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update an allocation")
    public ResponseEntity<AllocationDtos.AllocationResponse> update(@PathVariable String id, @Valid @RequestBody AllocationDtos.AllocationRequest request) {
        return ResponseEntity.ok(allocationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an allocation")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        allocationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/workload/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get an employee's current workload / utilization (employees use /api/me/workload)")
    public ResponseEntity<AllocationDtos.WorkloadResponse> getWorkload(@PathVariable String employeeId) {
        return ResponseEntity.ok(allocationService.getWorkload(employeeId));
    }

    @GetMapping("/conflicts")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List every employee whose active allocations exceed 100% capacity")
    public ResponseEntity<List<AllocationDtos.WorkloadResponse>> getConflicts() {
        return ResponseEntity.ok(allocationService.detectConflicts());
    }
}
