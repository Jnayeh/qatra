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
                return jpaRepository.findReadByUserIdAndType(userId, type, pageable)
                        .stream().map(mapper::toDomain).toList();
            }
            return jpaRepository.findUnreadByUserIdAndType(userId, type, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        if (type != null) {
            return jpaRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        if (read != null && read) {
            return jpaRepository.findReadByUserId(userId, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        if (read != null && !read) {
            return jpaRepository.findUnreadByUserId(userId, pageable)
                    .stream().map(mapper::toDomain).toList();
        }
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByUserId(Long userId, NotificationType type, Boolean read) {
        if (type != null && read != null) {
            if (read) {
                return jpaRepository.countByUserIdAndType(userId, type) - jpaRepository.countUnreadByUserIdAndType(userId, type);
            }
            return jpaRepository.countUnreadByUserIdAndType(userId, type);
        }
        if (type != null) {
            return jpaRepository.countByUserIdAndType(userId, type);
        }
        if (read != null) {
            if (read) {
                return jpaRepository.countByUserId(userId) - jpaRepository.countUnreadByUserId(userId);
            }
            return jpaRepository.countUnreadByUserId(userId);
        }
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return jpaRepository.countUnreadByUserId(userId);
    }

    @Override
    public int markAllAsReadByUserId(Long userId) {
        return jpaRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public boolean existsByCorrelationId(String correlationId) {
        return jpaRepository.existsByCorrelationId(correlationId);
    }
}
