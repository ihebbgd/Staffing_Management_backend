package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientId(String recipientId);
}
