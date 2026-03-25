package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.UserDtos;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.UserRole;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.UserRepository;
import com.demo.staffing_management_backend.security.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin-only management of authentication accounts: roles, enable/disable, password reset,
 * and linking a login to an employee record. Disabling or resetting a password bumps the
 * user's token version, which immediately revokes any outstanding JWTs.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final int GENERATED_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public Page<UserDtos.UserResponse> getAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        Map<String, String> employeeIdByUserId = employeeRepository
                .findByUserIdIn(page.getContent().stream().map(User::getId).toList()).stream()
                .collect(Collectors.toMap(Employee::getUserId, Employee::getId, (a, b) -> a));
        return page.map(user -> toResponse(user, employeeIdByUserId.get(user.getId())));
    }

    public UserDtos.UserResponse getById(String id) {
        User user = findOrThrow(id);
        return toResponse(user, linkedEmployeeId(id));
    }

    public UserDtos.UserResponse changeRole(String id, UserRole role) {
        User user = findOrThrow(id);
        user.setRole(role);
        userRepository.save(user);
        auditService.record("CHANGE_USER_ROLE", "USER", id, "role -> " + role.name());
        return toResponse(user, linkedEmployeeId(id));
    }

    public UserDtos.UserResponse setEnabled(String id, boolean enabled) {
        User user = findOrThrow(id);
        user.setEnabled(enabled);
        if (!enabled) {
            user.setTokenVersion(user.getTokenVersion() + 1); // revoke active sessions on disable
        }
        userRepository.save(user);
        auditService.record(enabled ? "ENABLE_USER" : "DISABLE_USER", "USER", id, null);
        return toResponse(user, linkedEmployeeId(id));
    }

    public String resetPassword(String id) {
        User user = findOrThrow(id);
        String temporary = PasswordGenerator.generate(GENERATED_PASSWORD_LENGTH);
        user.setPassword(passwordEncoder.encode(temporary));
        user.setTokenVersion(user.getTokenVersion() + 1); // force re-login with the new password
        userRepository.save(user);
        auditService.record("RESET_USER_PASSWORD", "USER", id, null);
        return temporary;
    }

    public UserDtos.UserResponse linkEmployee(String userId, String employeeId) {
        findOrThrow(userId);
        Employee target = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id : " + employeeId));
        // Enforce 1:1 — detach any other employee currently linked to this user.
        employeeRepository.findByUserId(userId).ifPresent(existing -> {
            if (!existing.getId().equals(employeeId)) {
                existing.setUserId(null);
                employeeRepository.save(existing);
            }
        });
        target.setUserId(userId);
        employeeRepository.save(target);
        return getById(userId);
    }

    public UserDtos.UserResponse unlinkEmployee(String userId) {
        findOrThrow(userId);
        employeeRepository.findByUserId(userId).ifPresent(employee -> {
            employee.setUserId(null);
            employeeRepository.save(employee);
        });
        return getById(userId);
    }

    public void delete(String id) {
        User user = findOrThrow(id);
        employeeRepository.findByUserId(id).ifPresent(employee -> {
            employee.setUserId(null);
            employeeRepository.save(employee);
        });
        userRepository.delete(user);
        auditService.record("DELETE_USER", "USER", id, null);
    }

    private String linkedEmployeeId(String userId) {
        return employeeRepository.findByUserId(userId).map(Employee::getId).orElse(null);
    }

    private User findOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + id));
    }

    private UserDtos.UserResponse toResponse(User user, String employeeId) {
        return new UserDtos.UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole(), user.isEnabled(), employeeId, user.getCreatedAt());
    }
}
