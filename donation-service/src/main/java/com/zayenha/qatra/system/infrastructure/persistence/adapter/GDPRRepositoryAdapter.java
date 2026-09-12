package com.zayenha.qatra.system.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import com.zayenha.qatra.system.infrastructure.persistence.entity.GDPRDeletionRequestEntity;
import com.zayenha.qatra.system.infrastructure.persistence.repository.GDPRDeletionRequestJpaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        return jpaRepository.findByUser_IdAndStatus(userId, GDPRDeletionStatus.IN_PROGRESS).map(this::toDomain);
    }

    @Override
    public List<GDPRDeletionRequest> findByStatus(GDPRDeletionStatus status) {
        return jpaRepository.findByStatus(status).stream().map(this::toDomain).toList();
    }

    @Override
    public List<GDPRDeletionRequest> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<GDPRDeletionRequest> findAll(SearchCriteria criteria, GDPRDeletionStatus status) {
        Specification<GDPRDeletionRequestEntity> spec = (root, query, cb) -> {
            Predicate predicate = status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
            return predicate;
        };
        var pageable = PageRequest.of(criteria.page(), criteria.size(), buildSort(criteria.sortBy(), criteria.sortDirection()));
        var page = jpaRepository.findAll(spec, pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        var allowed = List.of("id", "status", "requestedAt", "processedAt");
        var field = allowed.contains(sortBy) ? sortBy : "id";
        var dir = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field);
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
