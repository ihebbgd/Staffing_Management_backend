package com.demo.staffing_management_backend.controller;


import com.demo.staffing_management_backend.dto.CertificationDtos;
import com.demo.staffing_management_backend.service.CertificationService;
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
@RequiredArgsConstructor
@RequestMapping("/api/certifications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Certification", description = "Manage employee certifications and expiry status")
public class CertificationController {
    private final CertificationService certificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create a certification")
    public ResponseEntity<CertificationDtos.CertificationResponse> create(@Valid @RequestBody CertificationDtos.CertificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(certificationService.create(request));
    }

    @GetMapping
    @Operation(summary = "List a page of certifications")
    public ResponseEntity<Page<CertificationDtos.CertificationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(certificationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one certification by id")
    public ResponseEntity<CertificationDtos.CertificationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(certificationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "List all certifications for a given employee")
    public ResponseEntity<List<CertificationDtos.CertificationResponse>> getByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(certificationService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update a certification")
    public ResponseEntity<CertificationDtos.CertificationResponse> update(@PathVariable String id, @Valid @RequestBody CertificationDtos.CertificationRequest request) {
        return ResponseEntity.ok(certificationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a certification")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        certificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
