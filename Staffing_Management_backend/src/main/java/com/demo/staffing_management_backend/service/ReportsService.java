package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.CertificationMapper;
import com.demo.staffing_management_backend.dto.CertificationDtos;
import com.demo.staffing_management_backend.dto.ReportDtos;
import com.demo.staffing_management_backend.model.Certification;
import com.demo.staffing_management_backend.model.enums.CertificationStatus;
import com.demo.staffing_management_backend.model.enums.UtilizationStatus;
import com.demo.staffing_management_backend.repository.CertificationRepository;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.demo.staffing_management_backend.Mappers.CertificationMapper.computeStatus;

@Service
@RequiredArgsConstructor
public class ReportsService {
    private final EmployeeRepository employeeRepository;
    private final CertificationRepository certificationRepository;
    private final CertificationMapper certificationMapper;
    private final WorkloadService workloadService;

    public List<ReportDtos.UtilizationReportRow> utilizationReport() {
        LocalDate today = LocalDate.now();
        Map<String, Double> hoursByEmployee = workloadService.activeHoursByEmployee(today);

        return employeeRepository.findByActiveTrue().stream().map(employee -> {
            double capacity = employee.getWeeklyCapacityHours();
            double rawHours = hoursByEmployee.getOrDefault(employee.getId(), 0.0);
            double utilization = capacity > 0 ? workloadService.round1(rawHours / capacity * 100.0) : 0.0;
            return new ReportDtos.UtilizationReportRow(
                    employee.getId(),
                    employee.getFirstName() + " " + employee.getLastName(),
                    capacity,
                    workloadService.round1(rawHours),
                    utilization,
                    UtilizationStatus.of(utilization));
        }).toList();
    }

    public ReportDtos.CertificationReport certificationReport() {
        Map<CertificationStatus, List<CertificationDtos.CertificationResponse>> byStatus =
                certificationRepository.findAll().stream()
                        .collect(Collectors.groupingBy(
                                (Certification c) -> computeStatus(c.getExpiryDate()),
                                Collectors.mapping(certificationMapper::toResponse, Collectors.toList())));

        List<CertificationDtos.CertificationResponse> active =
                byStatus.getOrDefault(CertificationStatus.ACTIVE, List.of());
        List<CertificationDtos.CertificationResponse> expiringSoon =
                byStatus.getOrDefault(CertificationStatus.EXPIRING_SOON, List.of());
        List<CertificationDtos.CertificationResponse> expired =
                byStatus.getOrDefault(CertificationStatus.EXPIRED, List.of());

        return new ReportDtos.CertificationReport(active.size(), expiringSoon.size(), expired.size(),
                active, expiringSoon, expired);
    }
}
