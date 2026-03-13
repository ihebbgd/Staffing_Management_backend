package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.UserRole;
import jakarta.validation.constraints.Email;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(String username,
                                  @Email String email,
                                  String password) {

    }

    public record LoginRequest(String username,
                               String password) {

    }

    public record AuthResponse(String token,
                               String username,
                               UserRole role) {

    }

}
