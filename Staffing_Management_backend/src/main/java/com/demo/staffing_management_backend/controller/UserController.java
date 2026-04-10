package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.UserDtos;
import com.demo.staffing_management_backend.security.AppUserPrincipal;
import com.demo.staffing_management_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Operation(summary = "List a page of users (optionally filtered by username/email search)")
    public ResponseEntity<Page<UserDtos.UserResponse>> getAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<UserDtos.UserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new login account (returns a generated temporary password)")
    public ResponseEntity<UserDtos.CreateUserResponse> create(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change a user's role")
    public ResponseEntity<UserDtos.UserResponse> changeRole(@PathVariable String id,
                                                            @Valid @RequestBody UserDtos.RoleUpdateRequest request,
                                                            @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(userService.changeRole(id, request.role(), principal.getUserId()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user (disabling revokes their active tokens)")
    public ResponseEntity<UserDtos.UserResponse> setStatus(@PathVariable String id,
                                                           @Valid @RequestBody UserDtos.StatusUpdateRequest request,
                                                           @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(userService.setEnabled(id, request.enabled(), principal.getUserId()));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset a user's password (accepts a chosen password, or generates one when omitted; revokes active tokens)")
    public ResponseEntity<UserDtos.PasswordResetResponse> resetPassword(@PathVariable String id,
                                                                        @RequestBody(required = false) UserDtos.PasswordResetRequest request,
                                                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        String chosenPassword = request != null ? request.password() : null;
        return ResponseEntity.ok(new UserDtos.PasswordResetResponse(
                userService.resetPassword(id, chosenPassword, principal.getUserId())));
    }

    @PutMapping("/{id}/employee")
    @Operation(summary = "Link this user to an employee record")
    public ResponseEntity<UserDtos.UserResponse> linkEmployee(@PathVariable String id,
                                                              @Valid @RequestBody UserDtos.LinkEmployeeRequest request,
                                                              @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(userService.linkEmployee(id, request.employeeId(), principal.getUserId()));
    }

    @DeleteMapping("/{id}/employee")
    @Operation(summary = "Unlink this user from its employee record")
    public ResponseEntity<UserDtos.UserResponse> unlinkEmployee(@PathVariable String id,
                                                                @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(userService.unlinkEmployee(id, principal.getUserId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user (unlinks any employee first)")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @AuthenticationPrincipal AppUserPrincipal principal) {
        userService.delete(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
