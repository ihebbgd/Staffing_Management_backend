package com.demo.staffing_management_backend.model;

import com.demo.staffing_management_backend.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private  String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Builder.Default
    private UserRole role= UserRole.EMPLOYEE;

    private boolean enabled;

    private Instant createdAt;


}

