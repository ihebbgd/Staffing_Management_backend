package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    /**
     * Creating an employee always provisions a linked login. {@code username} defaults to the email,
     * {@code password} is generated (and returned once) when omitted, and {@code role} defaults to EMPLOYEE.
     */
    public record EmployeeCreateRequest(@NotBlank String firstName,
                                        @NotBlank String lastName,
                                        @NotBlank @Email String email,
                                        String jobTitle,
                                        String department,
                                        @PositiveOrZero double weeklyCapacityHours,
                                        @PositiveOrZero double yearsOfExperience,
                                        Boolean active,
                                        String username,
                                        String password,
                                        UserRole role) {
    }

    /** Updates only the HR fields; the linked login is managed through /api/users. */
    public record EmployeeUpdateRequest(@NotBlank String firstName,
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

    /** Returned on creation so the admin can hand the credentials to the new employee. */
    public record EmployeeCreationResponse(EmployeeResponse employee,
                                           String username,
                                           String temporaryPassword) {
    }
}
