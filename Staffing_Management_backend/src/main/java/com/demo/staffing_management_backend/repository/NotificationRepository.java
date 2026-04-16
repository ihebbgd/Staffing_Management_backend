package com.demo.staffing_management_backend.repository;

import com.demo.staffing_management_backend.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByRecipientId(String recipientId, Pageable pageable);
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    // 'isRead' is the persisted field name; a derived query on a boolean-with-'is'-prefix is ambiguous,
    // so the unread count is expressed explicitly.
    @Query(value = "{ 'recipientId': ?0, 'isRead': false }", count = true)
    long countUnreadByRecipientId(String recipientId);
}
