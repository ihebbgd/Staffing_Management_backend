package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WorkloadService {
    private final AllocationRepository allocationRepository;

    public double utilizationPercent(Employee employee) {
        return utilizationPercent(employee, LocalDate.now());
    }

    public double utilizationPercent(Employee employee, LocalDate asOf) {
        double total = activeHours(employee.getId(), asOf);
        double capacity = employee.getWeeklyCapacityHours();
        return capacity > 0 ? round1(total / capacity * 100.0) : 0.0;
    }

    public double activeHours(String employeeId) {
        return activeHours(employeeId, LocalDate.now());
    }

    public double activeHours(String employeeId, LocalDate asOf) {
        return allocationRepository.findByEmployeeIdAndStatus(employeeId, AllocationStatus.ACTIVE)
                .stream()
                .filter(a -> overlaps(a, asOf))
                .mapToDouble(Allocation::getAllocatedHoursPerWeek)
                .sum();
    }

    public static boolean overlaps(Allocation allocation, LocalDate date) {
        boolean started = allocation.getStartDate() == null || !allocation.getStartDate().isAfter(date);
        boolean notEnded = allocation.getEndDate() == null || !allocation.getEndDate().isBefore(date);
        return started && notEnded;
    }

    public double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
