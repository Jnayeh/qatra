package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationType;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        var spec = NotificationSpec.build(userId, type, read);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findAll(spec, pageable)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByUserId(Long userId, NotificationType type, Boolean read) {
        return jpaRepository.count(NotificationSpec.build(userId, type, read));
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return jpaRepository.count(NotificationSpec.build(userId, null, false));
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
