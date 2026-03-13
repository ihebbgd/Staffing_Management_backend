package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.AllocationStatus;

import java.time.Instant;
import java.time.LocalDate;

public final class AllocationDtos {
    private AllocationDtos() {}

    public record AllocationRequest(String employeeId,
                                    String projectId,
                                    double allocatedHoursPerWeek,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    AllocationStatus status,
                                    String roleOnProject) {
    }

    public record AllocationResponse(String id,
                                     String employeeId,
                                     String projectId,
                                     double allocatedHoursPerWeek,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     AllocationStatus status,
                                     String roleOnProject,
                                     Instant createdAt,
                                     boolean overAllocationWarning,
                                     double employeeUtilizationPercent){

    }

    public record WorkloadResponse(
            String employeeId,
            String employeeName,
            double weeklyCapacityHours,
            double totalAllocatedHours,
            double utilizationPercent,
            boolean overAllocated) {
    }

}
