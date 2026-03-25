package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.EmployeeMapper;
import com.demo.staffing_management_backend.Mappers.ProjectMapper;
import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.dto.CertificationDtos;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.dto.EmployeeSkillDtos;
import com.demo.staffing_management_backend.dto.ProjectDtos;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Backs the personal dashboard of the authenticated employee. Every method resolves the caller's
 * user id to their linked {@link Employee} and only ever returns data belonging to that employee.
 */
@Service
@RequiredArgsConstructor
public class MeService {
    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final ProjectRepository projectRepository;
    private final AllocationService allocationService;
    private final CertificationService certificationService;
    private final EmployeeSkillService employeeSkillService;
    private final EmployeeMapper employeeMapper;
    private final ProjectMapper projectMapper;

    public EmployeeDtos.EmployeeResponse profile(String userId) {
        return employeeMapper.toResponse(requireEmployee(userId));
    }

    public List<AllocationDtos.AllocationResponse> allocations(String userId) {
        return allocationService.getByEmployee(requireEmployee(userId).getId());
    }

    public AllocationDtos.WorkloadResponse workload(String userId) {
        return allocationService.getWorkload(requireEmployee(userId).getId());
    }

    public List<CertificationDtos.CertificationResponse> certifications(String userId) {
        return certificationService.getByEmployee(requireEmployee(userId).getId());
    }

    public List<EmployeeSkillDtos.EmployeeSkillResponse> skills(String userId) {
        return employeeSkillService.getByEmployee(requireEmployee(userId).getId());
    }

    public List<ProjectDtos.ProjectResponse> projects(String userId) {
        Employee me = requireEmployee(userId);
        Set<String> projectIds = allocationRepository.findByEmployeeId(me.getId()).stream()
                .map(Allocation::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return projectRepository.findAllById(projectIds).stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    /** Teammates = everyone else currently allocated to a project the caller is currently allocated to. */
    public List<EmployeeDtos.EmployeeResponse> teammates(String userId) {
        Employee me = requireEmployee(userId);
        LocalDate today = LocalDate.now();
        Set<String> myProjectIds = allocationRepository
                .findByEmployeeIdAndStatus(me.getId(), AllocationStatus.ACTIVE).stream()
                .filter(a -> WorkloadService.overlaps(a, today))
                .map(Allocation::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (myProjectIds.isEmpty()) {
            return List.of();
        }
        Set<String> teammateIds = allocationRepository
                .findByProjectIdInAndStatus(myProjectIds, AllocationStatus.ACTIVE).stream()
                .filter(a -> WorkloadService.overlaps(a, today))
                .map(Allocation::getEmployeeId)
                .filter(id -> id != null && !id.equals(me.getId()))
                .collect(Collectors.toSet());
        return employeeRepository.findAllById(teammateIds).stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    private Employee requireEmployee(String userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No employee profile linked to the current user"));
    }
}
