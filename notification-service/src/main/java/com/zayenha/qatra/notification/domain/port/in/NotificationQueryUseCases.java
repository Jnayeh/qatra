package com.zayenha.qatra.notification.domain.port.in;

import com.zayenha.qatra.notification.application.dto.NotificationResponse;

import java.util.List;

public interface NotificationQueryUseCases {
    List<NotificationResponse> getUserNotifications(Long userId, String type, Boolean read, int page, int size);
    long countUserNotifications(Long userId, String type, Boolean read);
    long countUnread(Long userId);
    NotificationResponse markAsRead(Long notificationId, Long userId);
    int markAllAsRead(Long userId);
}