package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.application.dto.NotificationResponse;
import com.zayenha.qatra.notification.domain.exception.NotificationNotFoundException;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationStatus;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class NotificationQueryService {

    private final NotificationRepositoryPort notificationRepository;

    public NotificationQueryService(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getUserNotifications(Long userId, String type, Boolean read,
                                                            int page, int size) {
        return notificationRepository.findByUserId(userId, type, read, page, size)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long countUserNotifications(Long userId, String type, Boolean read) {
        return notificationRepository.countByUserId(userId, type, read);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.markRead();
        notificationRepository.save(notification);
        return toResponse(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getUserId(), n.getType(), n.getTitle(),
                n.getBody(), n.getData(), n.getStatus().name(),
                n.getCreatedAt(), n.getReadAt());
    }
}
