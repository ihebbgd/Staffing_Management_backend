package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.NotificationDtos;
import com.demo.staffing_management_backend.security.AppUserPrincipal;
import com.demo.staffing_management_backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Create and read in-app notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create a new notification")
    public ResponseEntity<NotificationDtos.NotificationResponse> create(@Valid @RequestBody NotificationDtos.NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get a page of all notifications")
    public ResponseEntity<Page<NotificationDtos.NotificationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAll(pageable));
    }

    @GetMapping("/me")
    @Operation(summary = "List the authenticated user's own notifications, newest first")
    public ResponseEntity<Page<NotificationDtos.NotificationResponse>> getMine(
            @AuthenticationPrincipal AppUserPrincipal principal, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMine(principal.getUserId(), pageable));
    }

    @GetMapping("/me/unread-count")
    @Operation(summary = "Count the authenticated user's unread notifications")
    public ResponseEntity<NotificationDtos.UnreadCountResponse> myUnreadCount(
            @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(new NotificationDtos.UnreadCountResponse(
                notificationService.countUnread(principal.getUserId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one notification by id (own notifications, unless ADMIN/MANAGER)")
    public ResponseEntity<NotificationDtos.NotificationResponse> getById(@PathVariable String id,
                                                                         @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getForCaller(id, principal.getUserId(), isPrivileged(principal)));
    }

    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "List a page of notifications for a given recipient (own, unless ADMIN/MANAGER)")
    public ResponseEntity<Page<NotificationDtos.NotificationResponse>> getByRecipient(@PathVariable String recipientId,
                                                                                      @AuthenticationPrincipal AppUserPrincipal principal,
                                                                                      Pageable pageable) {
        if (!isPrivileged(principal) && !Objects.equals(recipientId, principal.getUserId())) {
            throw new AccessDeniedException("You may only view your own notifications");
        }
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId, pageable));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read (own notifications, unless ADMIN/MANAGER)")
    public ResponseEntity<NotificationDtos.NotificationResponse> markAsRead(@PathVariable String id,
                                                                            @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(notificationService.markAsReadForCaller(id, principal.getUserId(), isPrivileged(principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a notification by id")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isPrivileged(AppUserPrincipal principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_MANAGER"));
    }
}
