package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.SkillDtos;
import com.demo.staffing_management_backend.service.SkillService;
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
@RequiredArgsConstructor
@RequestMapping("/api/skills")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Skills", description = "Catalogue of skills")
public class SkillController {
    private final SkillService skillService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create a new skill")
    public ResponseEntity<SkillDtos.SkillResponse> create(@Valid @RequestBody SkillDtos.SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get a page of skills")
    public ResponseEntity<Page<SkillDtos.SkillResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(skillService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a skill by id")
    public ResponseEntity<SkillDtos.SkillResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(skillService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update a skill by id")
    public ResponseEntity<SkillDtos.SkillResponse> update(@PathVariable String id, @Valid @RequestBody SkillDtos.SkillRequest request) {
        return ResponseEntity.ok(skillService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a skill by id")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
