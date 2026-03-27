package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.AllocationStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.time.LocalDate;

public final class AllocationDtos {
    private AllocationDtos() {}

    public record AllocationRequest(@NotBlank String employeeId,
                                    @NotBlank String projectId,
                                    @Positive double allocatedHoursPerWeek,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    AllocationStatus status,
                                    String roleOnProject) {

        @AssertTrue(message = "endDate must be on or after startDate")
        public boolean isDateRangeValid() {
            return startDate == null || endDate == null || !endDate.isBefore(startDate);
        }
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
