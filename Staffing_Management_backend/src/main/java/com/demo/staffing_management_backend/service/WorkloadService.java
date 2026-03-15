package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkloadService {
    private final AllocationRepository allocationRepository;

    public double utilizationPercent(Employee employee) {
        double total = activeHours(employee.getId());
        double capacity = employee.getWeeklyCapacityHours();
        return capacity > 0 ? round1(total / capacity * 100.0) : 0.0;
    }

    public double activeHours(String employeeId) {
        return allocationRepository.findByEmployeeIdAndStatus(employeeId, AllocationStatus.ACTIVE)
                .stream()
                .mapToDouble(Allocation::getAllocatedHoursPerWeek)
                .sum();
    }
    public double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }



}
