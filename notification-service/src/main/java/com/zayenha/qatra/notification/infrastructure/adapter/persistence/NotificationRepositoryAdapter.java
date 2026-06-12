package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationType;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository jpaRepository;
    private final NotificationMapper mapper;

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(notification)));
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId, NotificationType type, Boolean read, int page, int size) {
        var pageable = PageRequest.of(page, size);
        if (type != null && read != null) {
            if (read) {
                return jpaRepository.findReadInAppByUserIdAndType(userId, type, pageable)
                        .stream().map(mapper::toDomain).toList();
            }
            return jpaRepository.findUnreadInAppByUserIdAndType(userId, type, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        if (type != null) {
            return jpaRepository.findInAppByUserIdAndType(userId, type, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        if (Boolean.TRUE.equals(read)) {
            return jpaRepository.findReadInAppByUserId(userId, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        if (Boolean.FALSE.equals(read)) {
            return jpaRepository.findUnreadInAppByUserId(userId, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        return jpaRepository.findInAppByUserId(userId, pageable)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByUserId(Long userId, NotificationType type, Boolean read) {
        if (type != null && read != null) {
            if (read) {
                return jpaRepository.countInAppByUserIdAndType(userId, type) - jpaRepository.countUnreadInAppByUserIdAndType(userId, type);
            }
            return jpaRepository.countUnreadInAppByUserIdAndType(userId, type);
        }
        if (type != null) {
            return jpaRepository.countInAppByUserIdAndType(userId, type);
        }
        if (read != null) {
            if (read) {
                return jpaRepository.countInAppByUserId(userId) - jpaRepository.countUnreadInAppByUserId(userId);
            }
            return jpaRepository.countUnreadInAppByUserId(userId);
        }
        return jpaRepository.countInAppByUserId(userId);
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return jpaRepository.countUnreadInAppByUserId(userId);
    }

    @Override
    public int markAllAsReadByUserId(Long userId) {
        return jpaRepository.markAllInAppAsReadByUserId(userId);
    }

    @Override
    public boolean existsByCorrelationId(String correlationId) {
        return jpaRepository.existsByCorrelationId(correlationId);
    }
}
