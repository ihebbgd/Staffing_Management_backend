package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.service.AllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<AllocationDtos.AllocationResponse> create(@RequestBody AllocationDtos.AllocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(allocationService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all allocations")
    public ResponseEntity<List<AllocationDtos.AllocationResponse>> getAll() {
        return ResponseEntity.ok(allocationService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one allocation by id")
    public ResponseEntity<AllocationDtos.AllocationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(allocationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "List all allocations for a given employee")
    public ResponseEntity<List<AllocationDtos.AllocationResponse>> getByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(allocationService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update an allocation")
    public ResponseEntity<AllocationDtos.AllocationResponse> update(@PathVariable String id, @RequestBody AllocationDtos.AllocationRequest request) {
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
    @Operation(summary = "Get an employee's current workload / utilization percentage")
    public ResponseEntity<AllocationDtos.WorkloadResponse> getWorkload(@PathVariable String employeeId) {
        return ResponseEntity.ok(allocationService.getWorkload(employeeId));
    }

    @GetMapping("/conflicts")
    @Operation(summary = "List every employee whose active allocations exceed 100% capacity")
    public ResponseEntity<List<AllocationDtos.WorkloadResponse>> getConflicts() {
        return ResponseEntity.ok(allocationService.detectConflicts());
    }

}
