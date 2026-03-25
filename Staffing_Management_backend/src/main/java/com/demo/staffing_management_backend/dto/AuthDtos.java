package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.UserRole;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken) {
    }

    public record AuthResponse(String accessToken,
                               String refreshToken,
                               String username,
                               UserRole role) {
    }
}
