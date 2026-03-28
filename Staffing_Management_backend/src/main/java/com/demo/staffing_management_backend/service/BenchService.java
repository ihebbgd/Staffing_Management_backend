package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.EmployeeMapper;
import com.demo.staffing_management_backend.dto.EmployeeDtos;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BenchService {
    private final EmployeeRepository employeeRepository;
    private final WorkloadService workloadService;
    private final EmployeeMapper employeeMapper;

    /**
     * Bench = active employees with no current ACTIVE allocation. The set of currently-allocated employee ids
     * is exactly the keys of WorkloadService's active-hours map, so we reuse that single source of truth.
     */
    public List<EmployeeDtos.EmployeeResponse> getBench() {
        LocalDate today = LocalDate.now();
        Set<String> allocatedEmployeeIds = workloadService.activeHoursByEmployee(today).keySet();
        return employeeRepository.findByActiveTrue().stream()
                .filter(employee -> !allocatedEmployeeIds.contains(employee.getId()))
                .map(employeeMapper::toResponse)
                .toList();
    }
}
