package com.demo.staffing_management_backend.dto;

import java.time.Instant;

public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    public record  EmployeeRequest(String firstName,
                                   String lastName,
                                   String email,
                                   String jobTitle,
                                   String department,
                                   double weeklyCapacityHours,
                                   double yearsOfExperience,
                                   Boolean active) {
    }

    public record EmployeeResponse(String id,
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
