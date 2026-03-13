package com.demo.staffing_management_backend.dto;

import com.demo.staffing_management_backend.model.enums.UserRole;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(String username,
                                  String email,
                                  String password,
                                  UserRole role) {

    }

    public record LoginRequest(String username,
                               String password) {

    }

    public record AuthResponse(String token,
                               String username,
                               UserRole role) {

    }

}
