package com.zayenha.qatra.system.infrastructure.persistence.adapter;

import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import com.zayenha.qatra.system.infrastructure.persistence.entity.GDPRDeletionRequestEntity;
import com.zayenha.qatra.system.infrastructure.persistence.repository.GDPRDeletionRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GDPRRepositoryAdapter implements GDPRRepositoryPort {

    private final GDPRDeletionRequestJpaRepository jpaRepository;

    @Override
    public GDPRDeletionRequest save(GDPRDeletionRequest request) {
        var entity = toEntity(request);
        if (entity.getId() != null) {
            var existing = jpaRepository.findById(entity.getId()).orElseThrow();
            existing.setStatus(entity.getStatus());
            existing.setProcessedAt(entity.getProcessedAt());
            return toDomain(jpaRepository.save(existing));
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<GDPRDeletionRequest> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<GDPRDeletionRequest> findByUserId(Long userId) {
        return jpaRepository.findByUser_Id(userId).map(this::toDomain);
    }

    @Override
    public List<GDPRDeletionRequest> findByStatus(GDPRDeletionStatus status) {
        return jpaRepository.findByStatus(status).stream().map(this::toDomain).toList();
    }

    @Override
    public List<GDPRDeletionRequest> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private GDPRDeletionRequestEntity toEntity(GDPRDeletionRequest domain) {
        var entity = new GDPRDeletionRequestEntity();
        entity.setId(domain.getId());
        entity.setUser(new com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity(domain.getUserId()));
        entity.setReason(domain.getReason());
        entity.setStatus(domain.getStatus());
        entity.setProcessedAt(domain.getProcessedAt());
        return entity;
    }

    private GDPRDeletionRequest toDomain(GDPRDeletionRequestEntity entity) {
        var domain = new GDPRDeletionRequest();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUser().getId());
        domain.setReason(entity.getReason());
        domain.setStatus(entity.getStatus());
        domain.setRequestedAt(entity.getRequestedAt());
        domain.setProcessedAt(entity.getProcessedAt());
        return domain;
    }
}
