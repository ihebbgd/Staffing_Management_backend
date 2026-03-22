package com.demo.staffing_management_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    public record EmployeeRequest(@NotBlank String firstName,
                                  @NotBlank String lastName,
                                  @NotBlank @Email String email,
                                  String jobTitle,
                                  String department,
                                  @PositiveOrZero double weeklyCapacityHours,
                                  @PositiveOrZero double yearsOfExperience,
                                  Boolean active) {
    }

    public record EmployeeResponse(String id,
                                   String userId,
                                   String firstName,
                                   String lastName,
                                   String email,
                                   String jobTitle,
                                   String department,
                                   double weeklyCapacityHours,
                                   double yearsOfExperience,
                                   boolean active,
                                   Instant createdAt) {

    }

}
