package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.NotificationType;
import org.springframework.data.jpa.domain.Specification;

public final class NotificationSpec {

    private NotificationSpec() {
    }

    public static Specification<NotificationEntity> inAppByUserId(Long userId) {
        return (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.like(root.get("channels"), "%IN_APP%")
                );
    }

    public static Specification<NotificationEntity> withType(NotificationType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<NotificationEntity> unread() {
        return (root, query, cb) -> cb.isNull(root.get("readAt"));
    }

    public static Specification<NotificationEntity> read() {
        return (root, query, cb) -> cb.isNotNull(root.get("readAt"));
    }

    public static Specification<NotificationEntity> build(Long userId, NotificationType type, Boolean read) {
        Specification<NotificationEntity> spec = inAppByUserId(userId);
        if (type != null) {
            spec = spec.and(withType(type));
        }
        if (Boolean.TRUE.equals(read)) {
            spec = spec.and(read());
        } else if (Boolean.FALSE.equals(read)) {
            spec = spec.and(unread());
        }
        return spec;
    }
}
