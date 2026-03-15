package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.AllocationMapper;
import com.demo.staffing_management_backend.dto.AllocationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.Project;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AllocationService {
    private final AllocationRepository allocationRepository;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final WorkloadService workloadService;
    private final AllocationMapper allocationMapper;


    public AllocationDtos.AllocationResponse create(AllocationDtos.AllocationRequest request) {
        if(request.allocatedHoursPerWeek()<=0){
            throw new BadRequestException("Allocated hours per week must be greater than 0");
        }
        Employee employee = employeeService.findOrThrow(request.employeeId());
        Project project = projectService.findOrThrow(request.projectId());

        Allocation allocation = Allocation.builder()
                .employeeId(request.employeeId())
                .projectId(request.projectId())
                .allocatedHoursPerWeek(request.allocatedHoursPerWeek())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status((request.status() != null ? request.status() : AllocationStatus.ACTIVE))
                .roleOnProject(request.roleOnProject())
                .createdAt(Instant.now())
                .build();

        Allocation saved=allocationRepository.save(allocation);
        double utilization=workloadService.utilizationPercent(employee);
        boolean over=utilization>100.0;
        if(over){
            notificationService.createSystemNotification(null,"Over allocation warning","Employee "+employee.getFirstName()+" " +employee.getLastName()+
                    " is now allocated to " +utilization+"% of weekly capacity","ALLOCATION_CONFLICT");
        }
        return allocationMapper.toResponse(saved,over,utilization);
    }
    public List<AllocationDtos.AllocationResponse> getAll() {
        return allocationRepository.findAll().stream().map(allocationMapper::toResponse).toList();
    }
    public AllocationDtos.AllocationResponse getById(String id) {
        return allocationMapper.toResponse(findOrThrow(id));
    }

    public List<AllocationDtos.AllocationResponse> getByEmployee(String employeeId) {
        return allocationRepository.findByEmployeeId(employeeId).stream().map(allocationMapper::toResponse).toList();
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
        Map<String, Double> hoursByEmployee = new HashMap<>();
        for (Allocation a : allocationRepository.findAll()) {
            if (a.getStatus() == AllocationStatus.ACTIVE) {
                hoursByEmployee.merge(a.getEmployeeId(), a.getAllocatedHoursPerWeek(), Double::sum);
            }
        }
        List<AllocationDtos.WorkloadResponse> conflicts = new ArrayList<>();
        for (Map.Entry<String, Double> entry : hoursByEmployee.entrySet()) {
            Employee employee = employeeService.findOrThrow(entry.getKey());
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

    private Allocation findOrThrow(String id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + id));
    }
}
