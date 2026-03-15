package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.NotificationMapper;
import com.demo.staffing_management_backend.dto.NotificationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Notification;
import com.demo.staffing_management_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationDtos.NotificationResponse create(NotificationDtos.NotificationRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("Notification title is required");
        }
        Notification notification = Notification.builder()
                .recipientId(request.recipientId())
                .title(request.title())
                .message(request.message())
                .type(request.type())
                .isRead(false)
                .createdAt(Instant.now())
                .build();
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    public Notification createSystemNotification(String recipientId, String title, String message,String type) {
        Notification notification=Notification.builder()
                .recipientId(recipientId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(Instant.now())
                .build();
        return notificationRepository.save(notification);
    }

    public List<NotificationDtos.NotificationResponse> getAll() {
        return notificationRepository.findAll().stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public NotificationDtos.NotificationResponse getById(String id) {
        return notificationMapper.toResponse(findOrThrow(id));
    }

    public List<NotificationDtos.NotificationResponse> getByRecipient(String recipientId) {
        return notificationRepository.findByRecipientId(recipientId).stream().map(notificationMapper::toResponse).toList();
    }

    public NotificationDtos.NotificationResponse markAsRead(String id) {
        Notification notification = findOrThrow(id);
        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }
    public void delete(String id) {
        Notification notification = findOrThrow(id);
        notificationRepository.delete(notification);
    }

    private Notification findOrThrow(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found : "+id));
    }











}

