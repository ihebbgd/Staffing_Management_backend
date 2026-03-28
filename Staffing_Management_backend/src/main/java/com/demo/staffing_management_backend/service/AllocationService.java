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
import org.springframework.data.domain.PageImpl;
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
        if (over) {
            String recipient = employee.getUserId() != null ? employee.getUserId() : employee.getId();
            notificationService.createSystemNotification(recipient, "Over-allocation warning",
                    "Employee " + employee.getFirstName() + " " + employee.getLastName()
                            + " is now allocated to " + utilization + "% of weekly capacity",
                    "ALLOCATION_CONFLICT");
        }
        return allocationMapper.toResponse(saved, over, utilization);
    }

    public Page<AllocationDtos.AllocationResponse> getAll(Pageable pageable) {
        Page<Allocation> page = allocationRepository.findAll(pageable);
        List<AllocationDtos.AllocationResponse> content = mapWithUtilization(page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public AllocationDtos.AllocationResponse getById(String id) {
        Allocation allocation = findOrThrow(id);
        Employee employee = employeeService.findOrThrow(allocation.getEmployeeId());
        double utilization = workloadService.utilizationPercent(employee);
        return allocationMapper.toResponse(allocation, utilization > 100.0, utilization);
    }

    public List<AllocationDtos.AllocationResponse> getByEmployee(String employeeId) {
        return mapWithUtilization(allocationRepository.findByEmployeeId(employeeId));
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

    private List<AllocationDtos.AllocationResponse> mapWithUtilization(List<Allocation> allocations) {
        if (allocations.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        Set<String> employeeIds = allocations.stream()
                .map(Allocation::getEmployeeId)
                .collect(Collectors.toSet());

        Map<String, Employee> employees = employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        Map<String, Double> activeHours = workloadService.activeHoursByEmployee(employeeIds, today);

        return allocations.stream().map(a -> {
            Employee employee = employees.get(a.getEmployeeId());
            double capacity = employee != null ? employee.getWeeklyCapacityHours() : 0.0;
            double percent = capacity > 0
                    ? workloadService.round1(activeHours.getOrDefault(a.getEmployeeId(), 0.0) / capacity * 100.0)
                    : 0.0;
            return allocationMapper.toResponse(a, percent > 100.0, percent);
        }).toList();
    }

    private Allocation findOrThrow(String id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + id));
    }
}
