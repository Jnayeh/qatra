package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository jpaRepository;

    public NotificationRepositoryAdapter(JpaNotificationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        var entity = toEntity(notification);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId, String type, Boolean read, int page, int size) {
        var pageable = PageRequest.of(page, size);
        if (type != null && read != null) {
            // ponytail: combined type+read filter not directly supported; falls back to type filter
            return jpaRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
                    .stream().map(this::toDomain).toList();
        }
        if (type != null) {
            return jpaRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
                    .stream().map(this::toDomain).toList();
        }
        if (read != null && read) {
            return jpaRepository.findReadByUserId(userId, pageable)
                    .stream().map(this::toDomain).toList();
        }
        if (read != null && !read) {
            return jpaRepository.findUnreadByUserId(userId, pageable)
                    .stream().map(this::toDomain).toList();
        }
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countByUserId(Long userId, String type, Boolean read) {
        if (type != null && read != null) {
            if (read) {
                return jpaRepository.countByUserId(userId) - jpaRepository.countUnreadByUserIdAndType(userId, type);
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

    private NotificationEntity toEntity(Notification domain) {
        var entity = new NotificationEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setType(domain.getType());
        entity.setTitle(domain.getTitle());
        entity.setBody(domain.getBody());
        entity.setData(domain.getData());
        entity.setCorrelationId(domain.getCorrelationId());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setReadAt(domain.getReadAt());
        return entity;
    }

    private Notification toDomain(NotificationEntity entity) {
        var n = new Notification();
        n.setId(entity.getId());
        n.setUserId(entity.getUserId());
        n.setTitle(entity.getTitle());
        n.setBody(entity.getBody());
        n.setData(entity.getData());
        n.setCorrelationId(entity.getCorrelationId());
        n.setStatus(entity.getStatus());
        n.setCreatedAt(entity.getCreatedAt());
        n.setReadAt(entity.getReadAt());
        return n;
    }
}
