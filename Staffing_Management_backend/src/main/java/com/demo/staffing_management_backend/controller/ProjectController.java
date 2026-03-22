package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.ProjectDtos;
import com.demo.staffing_management_backend.service.ProjectService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Projects", description = "Project management")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create a project")
    public ResponseEntity<ProjectDtos.ProjectResponse> create(@Valid @RequestBody ProjectDtos.ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @GetMapping
    @Operation(summary = "List a page of projects")
    public ResponseEntity<Page<ProjectDtos.ProjectResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(projectService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one project by id")
    public ResponseEntity<ProjectDtos.ProjectResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update a project")
    public ResponseEntity<ProjectDtos.ProjectResponse> update(@PathVariable String id,
                                                              @Valid @RequestBody ProjectDtos.ProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
