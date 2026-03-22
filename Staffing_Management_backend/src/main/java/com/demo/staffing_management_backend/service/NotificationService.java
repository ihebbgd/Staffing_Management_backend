package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.Mappers.NotificationMapper;
import com.demo.staffing_management_backend.dto.NotificationDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.Notification;
import com.demo.staffing_management_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
                .build();
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    public Notification createSystemNotification(String recipientId, String title, String message, String type) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    public Page<NotificationDtos.NotificationResponse> getAll(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(notificationMapper::toResponse);
    }

    public NotificationDtos.NotificationResponse getForCaller(String id, String callerId, boolean privileged) {
        Notification notification = findOrThrow(id);
        ensureAccess(notification, callerId, privileged);
        return notificationMapper.toResponse(notification);
    }

    public List<NotificationDtos.NotificationResponse> getByRecipient(String recipientId) {
        return notificationRepository.findByRecipientId(recipientId).stream()
                .map(notificationMapper::toResponse).toList();
    }

    public NotificationDtos.NotificationResponse markAsReadForCaller(String id, String callerId, boolean privileged) {
        Notification notification = findOrThrow(id);
        ensureAccess(notification, callerId, privileged);
        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    public void delete(String id) {
        Notification notification = findOrThrow(id);
        notificationRepository.delete(notification);
    }

    private void ensureAccess(Notification notification, String callerId, boolean privileged) {
        if (!privileged && !Objects.equals(notification.getRecipientId(), callerId)) {
            throw new AccessDeniedException("You do not have access to this notification");
        }
    }

    private Notification findOrThrow(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found : " + id));
    }
}
