package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.UtilizationStatus;

import java.util.List;

public final class ReportDtos {

    private ReportDtos() {
    }

    public record UtilizationReportRow(String employeeId,
                                       String employeeName,
                                       double weeklyCapacityHours,
                                       double allocatedHours,
                                       double utilizationPercent,
                                       UtilizationStatus status) {
    }

    public record CertificationReport(long activeCount,
                                      long expiringSoonCount,
                                      long expiredCount,
                                      List<CertificationDtos.CertificationResponse> active,
                                      List<CertificationDtos.CertificationResponse> expiringSoon,
                                      List<CertificationDtos.CertificationResponse> expired) {
    }
}
