package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.UserDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.DuplicateResourceException;
import com.demo.staffing_management_backend.exception.ForbiddenOperationException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Employee;
import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.UserRole;
import com.demo.staffing_management_backend.repository.EmployeeRepository;
import com.demo.staffing_management_backend.repository.UserRepository;
import com.demo.staffing_management_backend.security.PasswordGenerator;
import com.demo.staffing_management_backend.security.PasswordPolicy;
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
    private static final int MIN_LOGIN_PASSWORD_LENGTH = PasswordPolicy.MIN_LENGTH;

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public Page<UserDtos.UserResponse> getAll(String search, Pageable pageable) {
        String term = search == null ? "" : search.trim();
        Page<User> page = term.isEmpty()
                ? userRepository.findAll(pageable)
                : userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term, pageable);
        Map<String, String> employeeIdByUserId = employeeRepository
                .findByUserIdIn(page.getContent().stream().map(User::getId).toList()).stream()
                .collect(Collectors.toMap(Employee::getUserId, Employee::getId, (a, b) -> a));
        return page.map(user -> toResponse(user, employeeIdByUserId.get(user.getId())));
    }

    public UserDtos.UserResponse getById(String id) {
        User user = findOrThrow(id);
        return toResponse(user, linkedEmployeeId(id));
    }

    public UserDtos.CreateUserResponse create(UserDtos.CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already in use : " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use : " + request.email());
        }
        // Mirror the employee flow: use the admin's chosen password when supplied, otherwise generate one.
        ResolvedPassword password = resolvePassword(request.password());
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(password.raw()))
                .role(request.role())
                .enabled(true)
                .tokenVersion(0)
                .build();
        userRepository.save(user);
        auditService.record("CREATE_USER", "USER", user.getId(), "role -> " + request.role().name());
        // Only reveal the password when the server generated it; if the admin chose it, they already know it.
        return new UserDtos.CreateUserResponse(toResponse(user, null), password.revealable());
    }

    public UserDtos.UserResponse changeRole(String id, UserRole role, String currentUserId) {
        User user = findOrThrow(id);
        assertEditable(user, currentUserId);
        user.setRole(role);
        userRepository.save(user);
        auditService.record("CHANGE_USER_ROLE", "USER", id, "role -> " + role.name());
        return toResponse(user, linkedEmployeeId(id));
    }

    public UserDtos.UserResponse setEnabled(String id, boolean enabled, String currentUserId) {
        User user = findOrThrow(id);
        assertEditable(user, currentUserId);
        user.setEnabled(enabled);
        if (!enabled) {
            user.setTokenVersion(user.getTokenVersion() + 1); // revoke active sessions on disable
        }
        userRepository.save(user);
        auditService.record(enabled ? "ENABLE_USER" : "DISABLE_USER", "USER", id, null);
        return toResponse(user, linkedEmployeeId(id));
    }

    public String resetPassword(String id, String providedPassword, String currentUserId) {
        User user = findOrThrow(id);
        assertEditable(user, currentUserId);
        ResolvedPassword password = resolvePassword(providedPassword); // same rules as user creation
        user.setPassword(passwordEncoder.encode(password.raw()));
        user.setTokenVersion(user.getTokenVersion() + 1); // force re-login with the new password
        userRepository.save(user);
        auditService.record("RESET_USER_PASSWORD", "USER", id, null);
        return password.revealable();
    }

    public UserDtos.UserResponse linkEmployee(String userId, String employeeId, String currentUserId) {
        assertEditable(findOrThrow(userId), currentUserId);
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

    public UserDtos.UserResponse unlinkEmployee(String userId, String currentUserId) {
        assertEditable(findOrThrow(userId), currentUserId);
        employeeRepository.findByUserId(userId).ifPresent(employee -> {
            employee.setUserId(null);
            employeeRepository.save(employee);
        });
        return getById(userId);
    }

    public void delete(String id, String currentUserId) {
        User user = findOrThrow(id);
        assertDeletable(user, currentUserId);
        employeeRepository.findByUserId(id).ifPresent(employee -> {
            employee.setUserId(null);
            employeeRepository.save(employee);
        });
        userRepository.delete(user);
        auditService.record("DELETE_USER", "USER", id, null);
    }

    /**
     * An admin may edit any account (admin, manager or employee) except their own — refusing
     * self-edits guarantees an administrator can never strip their own privileges or lock
     * themselves out. This is the authoritative check; the frontend guard is only cosmetic.
     */
    private void assertEditable(User target, String currentUserId) {
        if (target.getId().equals(currentUserId)) {
            throw new ForbiddenOperationException("You cannot modify your own account");
        }
    }

    /**
     * Deletion adds two guards on top of the edit rules: an admin cannot delete their own
     * account, and the system refuses to remove the final administrator so it can never be
     * left with zero admins (which would make it unmanageable).
     */
    private void assertDeletable(User target, String currentUserId) {
        if (target.getId().equals(currentUserId)) {
            throw new ForbiddenOperationException("You cannot delete your own account");
        }
        if (target.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new ForbiddenOperationException("Cannot delete the last remaining administrator");
        }
    }

    /**
     * Single source of truth for the login-password rule, shared by user creation and password reset:
     * use the admin's chosen password when supplied (enforcing the minimum length), otherwise generate
     * a strong one. Callers encode the raw value and only ever expose a generated password.
     */
    private ResolvedPassword resolvePassword(String provided) {
        boolean generated = provided == null || provided.isBlank();
        String raw = generated ? PasswordGenerator.generate(GENERATED_PASSWORD_LENGTH) : provided;
        if (!generated && raw.length() < MIN_LOGIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be at least " + MIN_LOGIN_PASSWORD_LENGTH + " characters");
        }
        return new ResolvedPassword(raw, generated);
    }

    /** A resolved login password. {@code revealable()} returns the raw value only when it was generated. */
    private record ResolvedPassword(String raw, boolean generated) {
        String revealable() {
            return generated ? raw : null;
        }
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
