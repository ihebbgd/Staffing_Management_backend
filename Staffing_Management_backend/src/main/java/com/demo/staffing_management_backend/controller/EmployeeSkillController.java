package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.EmployeeSkillDtos;
import com.demo.staffing_management_backend.service.EmployeeSkillService;
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
@RequestMapping("/api/employee-skills")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Employee Skills", description = "Assign skills and proficiency levels to employees")
public class EmployeeSkillController {
    private final EmployeeSkillService employeeSkillService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Assign a skill to an employee")
    public ResponseEntity<EmployeeSkillDtos.EmployeeSkillResponse> create(@Valid @RequestBody EmployeeSkillDtos.EmployeeSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeSkillService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List a page of employee-skill links")
    public ResponseEntity<Page<EmployeeSkillDtos.EmployeeSkillResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(employeeSkillService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get one employee-skill link by id")
    public ResponseEntity<EmployeeSkillDtos.EmployeeSkillResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(employeeSkillService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List all skills for one employee (employees use /api/me/skills)")
    public ResponseEntity<List<EmployeeSkillDtos.EmployeeSkillResponse>> getByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeSkillService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update a proficiency level")
    public ResponseEntity<EmployeeSkillDtos.EmployeeSkillResponse> update(@PathVariable String id, @Valid @RequestBody EmployeeSkillDtos.EmployeeSkillRequest request) {
        return ResponseEntity.ok(employeeSkillService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Remove a skill from an employee")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        employeeSkillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
