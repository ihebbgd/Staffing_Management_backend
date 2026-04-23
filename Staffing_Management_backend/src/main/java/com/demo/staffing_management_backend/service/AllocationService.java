package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.AllocationMapper;
import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {
    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final WorkloadService workloadService;
    private final AllocationMapper allocationMapper;

    public AllocationDtos.AllocationResponse create(AllocationDtos.AllocationRequest request) {
        if (request.allocatedHoursPerWeek() <= 0) {
            throw new BadRequestException("Allocated hours per week must be greater than 0");
        }
        Employee employee = employeeService.findOrThrow(request.employeeId());
        projectService.findOrThrow(request.projectId());

        Allocation allocation = Allocation.builder()
                .employeeId(request.employeeId())
                .projectId(request.projectId())
                .allocatedHoursPerWeek(request.allocatedHoursPerWeek())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status() != null ? request.status() : AllocationStatus.ACTIVE)
                .roleOnProject(request.roleOnProject())
                .build();

        Allocation saved = allocationRepository.save(allocation);
        double utilization = workloadService.utilizationPercent(employee);
        boolean over = utilization > 100.0;
        // Notifications are addressed to a user account; skip when the employee has no linked login
        // (an employee id is not a valid notification recipient and would never be delivered).
        if (over && employee.getUserId() != null) {
            notificationService.createSystemNotification(employee.getUserId(), "Over-allocation warning",
                    "Employee " + employee.getFirstName() + " " + employee.getLastName()
                            + " is now allocated to " + utilization + "% of weekly capacity",
                    "ALLOCATION_CONFLICT");
        }
        return allocationMapper.toResponse(saved, over, utilization);
    }

    public Page<AllocationDtos.AllocationResponse> getAll(Pageable pageable) {
        Page<Allocation> page = allocationRepository.findAll(pageable);
        UtilizationView utilization = utilizationFor(page.getContent());
        // Map through Page so pagination metadata is preserved without building a bare PageImpl.
        return page.map(utilization::toResponse);
    }

    public AllocationDtos.AllocationResponse getById(String id) {
        Allocation allocation = findOrThrow(id);
        Employee employee = employeeService.findOrThrow(allocation.getEmployeeId());
        double utilization = workloadService.utilizationPercent(employee);
        return allocationMapper.toResponse(allocation, utilization > 100.0, utilization);
    }

    public List<AllocationDtos.AllocationResponse> getByEmployee(String employeeId) {
        List<Allocation> allocations = allocationRepository.findByEmployeeId(employeeId);
        UtilizationView utilization = utilizationFor(allocations);
        return allocations.stream().map(utilization::toResponse).toList();
    }

    public AllocationDtos.AllocationResponse update(String id, AllocationDtos.AllocationRequest request) {
        if (request.allocatedHoursPerWeek() <= 0) {
            throw new BadRequestException("allocatedHoursPerWeek must be greater than 0");
        }
        Allocation allocation = findOrThrow(id);
        Employee employee = employeeService.findOrThrow(request.employeeId());
        projectService.findOrThrow(request.projectId());

        allocation.setEmployeeId(request.employeeId());
        allocation.setProjectId(request.projectId());
        allocation.setAllocatedHoursPerWeek(request.allocatedHoursPerWeek());
        allocation.setStartDate(request.startDate());
        allocation.setEndDate(request.endDate());
        if (request.status() != null) {
            allocation.setStatus(request.status());
        }
        allocation.setRoleOnProject(request.roleOnProject());

        Allocation saved = allocationRepository.save(allocation);
        double utilization = workloadService.utilizationPercent(employee);
        return allocationMapper.toResponse(saved, utilization > 100.0, utilization);
    }

    public void delete(String id) {
        Allocation allocation = findOrThrow(id);
        allocationRepository.delete(allocation);
    }

    public AllocationDtos.WorkloadResponse getWorkload(String employeeId) {
        Employee employee = employeeService.findOrThrow(employeeId);
        double total = workloadService.activeHours(employeeId);
        double capacity = employee.getWeeklyCapacityHours();
        double percent = capacity > 0 ? workloadService.round1(total / capacity * 100.0) : 0.0;
        return new AllocationDtos.WorkloadResponse(
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                capacity,
                workloadService.round1(total),
                percent,
                percent > 100.0);
    }

    public List<AllocationDtos.WorkloadResponse> detectConflicts() {
        LocalDate today = LocalDate.now();
        Map<String, Double> hoursByEmployee = workloadService.activeHoursByEmployee(today);

        Map<String, Employee> employees = employeeRepository.findAllById(hoursByEmployee.keySet()).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        List<AllocationDtos.WorkloadResponse> conflicts = new ArrayList<>();
        for (Map.Entry<String, Double> entry : hoursByEmployee.entrySet()) {
            Employee employee = employees.get(entry.getKey());
            if (employee == null) {
                continue;
            }
            double total = entry.getValue();
            double capacity = employee.getWeeklyCapacityHours();
            double percent = capacity > 0 ? workloadService.round1(total / capacity * 100.0) : 0.0;
            if (percent > 100.0) {
                conflicts.add(new AllocationDtos.WorkloadResponse(
                        employee.getId(),
                        employee.getFirstName() + " " + employee.getLastName(),
                        capacity,
                        workloadService.round1(total),
                        percent,
                        true));
            }
        }
        return conflicts;
    }

    /** Batch-loads the employees and their active hours for a set of allocations, once. */
    private UtilizationView utilizationFor(List<Allocation> allocations) {
        if (allocations.isEmpty()) {
            return new UtilizationView(Map.of(), Map.of());
        }
        Set<String> employeeIds = allocations.stream()
                .map(Allocation::getEmployeeId)
                .collect(Collectors.toSet());
        Map<String, Employee> employees = employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));
        Map<String, Double> activeHours = workloadService.activeHoursByEmployee(employeeIds, LocalDate.now());
        return new UtilizationView(employees, activeHours);
    }

    /** Precomputed utilization context: maps one allocation to its response with the over-allocation flag. */
    private final class UtilizationView {
        private final Map<String, Employee> employees;
        private final Map<String, Double> activeHours;

        private UtilizationView(Map<String, Employee> employees, Map<String, Double> activeHours) {
            this.employees = employees;
            this.activeHours = activeHours;
        }

        private AllocationDtos.AllocationResponse toResponse(Allocation allocation) {
            Employee employee = employees.get(allocation.getEmployeeId());
            double capacity = employee != null ? employee.getWeeklyCapacityHours() : 0.0;
            double percent = capacity > 0
                    ? workloadService.round1(activeHours.getOrDefault(allocation.getEmployeeId(), 0.0) / capacity * 100.0)
                    : 0.0;
            return allocationMapper.toResponse(allocation, percent > 100.0, percent);
        }
    }

    private Allocation findOrThrow(String id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + id));
    }
}
