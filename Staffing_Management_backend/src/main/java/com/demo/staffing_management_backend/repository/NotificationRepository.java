package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByRecipientId(String recipientId, Pageable pageable);
}
