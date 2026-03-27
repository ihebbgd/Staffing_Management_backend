package com.demo.staffing_management_backend.model;

import com.demo.staffing_management_backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// Deliberately NOT @Data: no generated toString()/equals()/hashCode() so the password hash
// can never be leaked through logging or accidental string interpolation of a User.
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Builder.Default
    private UserRole role = UserRole.EMPLOYEE;

    private boolean enabled;

    @Builder.Default
    private int tokenVersion = 0;

    @CreatedDate
    private Instant createdAt;
}
