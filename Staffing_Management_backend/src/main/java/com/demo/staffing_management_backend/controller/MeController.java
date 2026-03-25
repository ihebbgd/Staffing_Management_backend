package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.dto.CertificationDtos;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.dto.EmployeeSkillDtos;
import com.demo.staffing_management_backend.dto.ProjectDtos;
import com.demo.staffing_management_backend.security.AppUserPrincipal;
import com.demo.staffing_management_backend.service.MeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Me", description = "Personal dashboard for the authenticated employee (their own data only)")
public class MeController {
    private final MeService meService;

    @GetMapping
    @Operation(summary = "My employee profile")
    public ResponseEntity<EmployeeDtos.EmployeeResponse> profile(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meService.profile(principal.getUserId()));
    }

    @GetMapping("/allocations")
    @Operation(summary = "My allocations")
    public ResponseEntity<List<AllocationDtos.AllocationResponse>> allocations(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meService.allocations(principal.getUserId()));
    }

    @GetMapping("/projects")
    @Operation(summary = "Projects I am allocated to")
    public ResponseEntity<List<ProjectDtos.ProjectResponse>> projects(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meService.projects(principal.getUserId()));
    }

    @GetMapping("/teammates")
    @Operation(summary = "People currently working on the same projects as me")
    public ResponseEntity<List<EmployeeDtos.EmployeeResponse>> teammates(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meService.teammates(principal.getUserId()));
    }

    @GetMapping("/certifications")
    @Operation(summary = "My certifications")
    public ResponseEntity<List<CertificationDtos.CertificationResponse>> certifications(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meService.certifications(principal.getUserId()));
    }

    @GetMapping("/skills")
    @Operation(summary = "My skills")
    public ResponseEntity<List<EmployeeSkillDtos.EmployeeSkillResponse>> skills(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meService.skills(principal.getUserId()));
    }

    @GetMapping("/workload")
    @Operation(summary = "My current workload / utilization")
    public ResponseEntity<AllocationDtos.WorkloadResponse> workload(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(meService.workload(principal.getUserId()));
    }
}
