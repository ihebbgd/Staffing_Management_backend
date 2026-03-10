package com.demo.staffing_management_backend.model;

import com.demo.staffing_management_backend.model.enums.UserRole;
import com.demo.staffing_management_backend.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    @Builder.Default
    private UserRole role= UserRole.EMPLOYEE;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    private String employeeId;

    private String refreshTokenHash;

    @Builder.Default
    private int failedAttempts = 0;

    private LocalDateTime lastLoginAt;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;

    @Builder.Default
    private boolean mustChangePassword = false;

    @CreatedDate private  LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
}

