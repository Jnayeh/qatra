package com.zayenha.qatra.notification.domain.port.out;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationType;

import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findByUserId(Long userId, NotificationType type, Boolean read, int page, int size);

    long countByUserId(Long userId, NotificationType type, Boolean read);

    long countUnreadByUserId(Long userId);

    int markAllAsReadByUserId(Long userId);

    boolean existsByCorrelationId(String correlationId);
}
