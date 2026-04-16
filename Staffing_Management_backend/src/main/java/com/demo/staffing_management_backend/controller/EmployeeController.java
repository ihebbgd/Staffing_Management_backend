package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.service.EmployeeService;
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

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Employee", description = "Employee management (ADMIN/MANAGER overview). Employees use /api/me for their own data.")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create an employee and provision their linked login (returns the temporary password once)")
    public ResponseEntity<EmployeeDtos.EmployeeCreationResponse> create(@Valid @RequestBody EmployeeDtos.EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get a page of employees (optionally filtered by name/email/department/title search)")
    public ResponseEntity<Page<EmployeeDtos.EmployeeResponse>> getAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAll(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get employee by id")
    public ResponseEntity<EmployeeDtos.EmployeeResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update an employee's HR fields (login is managed via /api/users)")
    public ResponseEntity<EmployeeDtos.EmployeeResponse> update(@PathVariable String id, @Valid @RequestBody EmployeeDtos.EmployeeUpdateRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an employee and its linked login")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
