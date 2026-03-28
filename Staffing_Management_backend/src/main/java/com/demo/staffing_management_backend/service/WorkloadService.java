package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Currently-active allocated hours per employee across the whole organisation, keyed by employee id.
     * "Active" = allocation status ACTIVE and the allocation window overlaps {@code asOf}. Employees with no
     * such allocation are absent from the map (so {@code keySet()} is exactly the currently-allocated employees).
     */
    public Map<String, Double> activeHoursByEmployee(LocalDate asOf) {
        return sumHoursByEmployee(allocationRepository.findByStatus(AllocationStatus.ACTIVE), asOf);
    }

    /** Same as {@link #activeHoursByEmployee(LocalDate)} but restricted to the given employee ids. */
    public Map<String, Double> activeHoursByEmployee(Collection<String> employeeIds, LocalDate asOf) {
        if (employeeIds.isEmpty()) {
            return Map.of();
        }
        return sumHoursByEmployee(
                allocationRepository.findByEmployeeIdInAndStatus(employeeIds, AllocationStatus.ACTIVE), asOf);
    }

    private Map<String, Double> sumHoursByEmployee(List<Allocation> activeAllocations, LocalDate asOf) {
        return activeAllocations.stream()
                .filter(a -> overlaps(a, asOf))
                .collect(Collectors.groupingBy(Allocation::getEmployeeId,
                        Collectors.summingDouble(Allocation::getAllocatedHoursPerWeek)));
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
