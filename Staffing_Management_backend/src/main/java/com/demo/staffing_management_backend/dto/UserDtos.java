package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserResponse(String id,
                               String username,
                               String email,
                               UserRole role,
                               boolean enabled,
                               String employeeId,
                               Instant createdAt) {
    }

    /**
     * {@code password} is optional and mirrors the employee-creation flow: when provided it is used
     * (after a minimum-length check) and encoded; when omitted a strong one is generated and returned once.
     */
    public record CreateUserRequest(@NotBlank String username,
                                    @NotBlank @Email String email,
                                    @NotNull UserRole role,
                                    String password) {
    }

    /** {@code temporaryPassword} is only populated when the server generated the password. */
    public record CreateUserResponse(UserResponse user, String temporaryPassword) {
    }

    public record RoleUpdateRequest(@NotNull UserRole role) {
    }

    public record StatusUpdateRequest(@NotNull Boolean enabled) {
    }

    public record LinkEmployeeRequest(@NotBlank String employeeId) {
    }

    /** Optional chosen password for a reset; when omitted the server generates one (same rule as create). */
    public record PasswordResetRequest(String password) {
    }

    /** {@code temporaryPassword} is only populated when the server generated the password. */
    public record PasswordResetResponse(String temporaryPassword) {
    }
}
