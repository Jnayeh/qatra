package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "email", source = "email")
    NotificationEntity toEntity(Notification domain);

    @Mapping(target = "email", source = "email")
    Notification toDomain(NotificationEntity entity);
}