package com.demo.staffing_management_backend.Mappers;

import com.demo.staffing_management_backend.dto.NotificationDtos;
import com.demo.staffing_management_backend.model.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationDtos.NotificationResponse toResponse(Notification notification);
}
