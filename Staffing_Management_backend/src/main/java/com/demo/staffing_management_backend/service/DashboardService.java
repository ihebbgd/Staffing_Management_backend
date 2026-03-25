package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.DashboardDtos;
import com.demo.staffing_management_backend.model.Allocation;
import com.demo.staffing_management_backend.model.Certification;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import com.demo.staffing_management_backend.model.enums.ProjectStatus;
import com.demo.staffing_management_backend.repository.AllocationRepository;
import com.demo.staffing_management_backend.repository.CertificationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.demo.staffing_management_backend.Mappers.CertificationMapper.computeStatus;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final int EXPIRY_WINDOW_60_DAYS = 60;

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final CertificationRepository certificationRepository;
    private final ProjectRepository projectRepository;
    private final WorkloadService workloadService;

    public DashboardDtos.StatsResponse getStats() {
        LocalDate today = LocalDate.now();

        long totalEmployees = employeeRepository.count();
        List<Employee> activeEmployees = employeeRepository.findByActiveTrue();

        // Active, currently-overlapping allocations grouped by employee (matches AllocationService).
        Map<String, Double> hoursByEmployee = allocationRepository.findByStatus(AllocationStatus.ACTIVE).stream()
                .filter(a -> WorkloadService.overlaps(a, today))
                .collect(Collectors.groupingBy(Allocation::getEmployeeId,
                        Collectors.summingDouble(Allocation::getAllocatedHoursPerWeek)));

        long allocatedEmployees = activeEmployees.stream()
                .filter(employee -> hoursByEmployee.containsKey(employee.getId()))
                .count();
        long benchCount = activeEmployees.size() - allocatedEmployees;
        double benchPercentage = activeEmployees.isEmpty() ? 0.0
                : workloadService.round1(benchCount * 100.0 / activeEmployees.size());

        double sumUtilization = 0.0;
        double sumHours = 0.0;
        double sumCapacity = 0.0;
        for (Employee employee : activeEmployees) {
            double capacity = employee.getWeeklyCapacityHours();
            double hours = hoursByEmployee.getOrDefault(employee.getId(), 0.0);
            sumHours += hours;
            sumCapacity += capacity;
            sumUtilization += capacity > 0 ? hours / capacity * 100.0 : 0.0;
        }
        double averageUtilizationPercent = activeEmployees.isEmpty() ? 0.0
                : workloadService.round1(sumUtilization / activeEmployees.size());
        double allocationRate = sumCapacity > 0 ? workloadService.round1(sumHours / sumCapacity * 100.0) : 0.0;

        long activeProjects = projectRepository.countByStatus(ProjectStatus.ACTIVE);

        List<Certification> certifications = certificationRepository.findAll();
        long expiredCertifications = certifications.stream()
                .filter(c -> computeStatus(c.getExpiryDate()) == CertificationStatus.EXPIRED).count();
        long expiringIn30Days = certifications.stream()
                .filter(c -> computeStatus(c.getExpiryDate()) == CertificationStatus.EXPIRING_SOON).count();
        long expiringIn60Days = certifications.stream()
                .filter(c -> expiringWithin(c.getExpiryDate(), today, EXPIRY_WINDOW_60_DAYS)).count();

        return new DashboardDtos.StatsResponse(totalEmployees, activeEmployees.size(), benchCount,
                benchPercentage, allocatedEmployees, activeProjects, averageUtilizationPercent,
                allocationRate, expiringIn30Days, expiringIn60Days, expiredCertifications);
    }

    /** Cumulative window: a non-expired certification whose expiry falls within [today, today + days]. */
    private boolean expiringWithin(LocalDate expiryDate, LocalDate today, int days) {
        return expiryDate != null && !expiryDate.isBefore(today) && !expiryDate.isAfter(today.plusDays(days));
    }
}
