package com.demo.staffing_management_backend.dto;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record StatsResponse(long totalEmployees,
                                long activeEmployees,
                                long benchCount,
                                double benchPercentage,
                                long allocatedEmployees,
                                long activeProjects,
                                double averageUtilizationPercent,
                                double allocationRate,
                                long certificationsExpiringIn30Days,
                                long certificationsExpiringIn60Days,
                                long expiredCertifications) {
    }
}
