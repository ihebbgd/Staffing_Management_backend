package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.UserDtos;
import com.demo.staffing_management_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "Admin management of login accounts (roles, status, password reset, employee link)")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "List a page of users")
    public ResponseEntity<Page<UserDtos.UserResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<UserDtos.UserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change a user's role")
    public ResponseEntity<UserDtos.UserResponse> changeRole(@PathVariable String id,
                                                            @Valid @RequestBody UserDtos.RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.changeRole(id, request.role()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user (disabling revokes their active tokens)")
    public ResponseEntity<UserDtos.UserResponse> setStatus(@PathVariable String id,
                                                           @Valid @RequestBody UserDtos.StatusUpdateRequest request) {
        return ResponseEntity.ok(userService.setEnabled(id, request.enabled()));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset a user's password to a new temporary one (revokes active tokens)")
    public ResponseEntity<UserDtos.PasswordResetResponse> resetPassword(@PathVariable String id) {
        return ResponseEntity.ok(new UserDtos.PasswordResetResponse(userService.resetPassword(id)));
    }

    @PutMapping("/{id}/employee")
    @Operation(summary = "Link this user to an employee record")
    public ResponseEntity<UserDtos.UserResponse> linkEmployee(@PathVariable String id,
                                                              @Valid @RequestBody UserDtos.LinkEmployeeRequest request) {
        return ResponseEntity.ok(userService.linkEmployee(id, request.employeeId()));
    }

    @DeleteMapping("/{id}/employee")
    @Operation(summary = "Unlink this user from its employee record")
    public ResponseEntity<UserDtos.UserResponse> unlinkEmployee(@PathVariable String id) {
        return ResponseEntity.ok(userService.unlinkEmployee(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user (unlinks any employee first)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
