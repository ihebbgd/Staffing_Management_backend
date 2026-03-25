package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.UserRole;
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

    public record RoleUpdateRequest(@NotNull UserRole role) {
    }

    public record StatusUpdateRequest(@NotNull Boolean enabled) {
    }

    public record LinkEmployeeRequest(@NotBlank String employeeId) {
    }

    public record PasswordResetResponse(String temporaryPassword) {
    }
}
