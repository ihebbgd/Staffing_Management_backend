package com.demo.staffing_management_backend.controller;

import com.demo.staffing_management_backend.dto.NotificationDtos;
import com.demo.staffing_management_backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<NotificationDtos.NotificationResponse> create(@RequestBody NotificationDtos.NotificationRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<NotificationDtos.NotificationResponse>> getAll(){
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one notification by id")
    public ResponseEntity<NotificationDtos.NotificationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }
    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "List all notifications for a given recipient")
    public ResponseEntity<List<NotificationDtos.NotificationResponse>> getByRecipient(@PathVariable String recipientId) {
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId));
    }
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationDtos.NotificationResponse> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification by id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
