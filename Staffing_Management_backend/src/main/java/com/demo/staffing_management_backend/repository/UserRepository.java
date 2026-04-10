package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByRole(UserRole role);
    long countByRole(UserRole role);

    // Case-insensitive substring search across username and email (backs the Users page search box).
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);
}
