package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.ProjectStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatus(ProjectStatus status);
    Optional<User> findByPasswordResetToken(String passwordResetToken);
}
