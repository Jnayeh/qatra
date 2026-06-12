package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "email", source = "email")
    @Mapping(target = "channels", expression = "java(joinChannels(domain.getChannels()))")
    NotificationEntity toEntity(Notification domain);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "channels", expression = "java(splitChannels(entity.getChannels()))")
    Notification toDomain(NotificationEntity entity);

    default String joinChannels(List<NotificationChannel> channels) {
        if (channels == null || channels.isEmpty()) return "";
        return channels.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    default List<NotificationChannel> splitChannels(String channels) {
        if (channels == null || channels.isBlank()) return List.of();
        return Arrays.stream(channels.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toList());
    }
}