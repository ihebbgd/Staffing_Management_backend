package com.demo.staffing_management_backend.controller;


import com.demo.staffing_management_backend.dto.RecommendationDtos;
import com.demo.staffing_management_backend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recommendations", description = "AI-style scoring of employees for a project")
public class RecommendationController {

    private final RecommendationService recommendationService;
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get the top-N recommended employees for a project, with score breakdown")
    public ResponseEntity<List<RecommendationDtos.RecommendationResult>> recommendForProject(@PathVariable String projectId, @RequestParam(defaultValue = "5") int topN) {
        return ResponseEntity.ok(recommendationService.recommendForProject(projectId, topN));
    }
}
