package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.EmployeeMapper;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BenchService {
    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final EmployeeMapper employeeMapper;

    /**
     * Bench = active employees with no current ACTIVE allocation. Batched: load active allocations once,
     * derive the allocated employeeIds in memory (no per-employee query), then filter the active employees.
     */
    public List<EmployeeDtos.EmployeeResponse> getBench() {
        LocalDate today = LocalDate.now();
        Set<String> allocatedEmployeeIds = allocationRepository.findByStatus(AllocationStatus.ACTIVE).stream()
                .filter(a -> WorkloadService.overlaps(a, today))
                .map(Allocation::getEmployeeId)
                .collect(Collectors.toSet());
        return employeeRepository.findByActiveTrue().stream()
                .filter(employee -> !allocatedEmployeeIds.contains(employee.getId()))
                .map(employeeMapper::toResponse)
                .toList();
    }
}
