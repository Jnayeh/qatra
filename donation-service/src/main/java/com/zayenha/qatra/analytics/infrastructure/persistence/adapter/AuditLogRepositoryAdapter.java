package com.zayenha.qatra.analytics.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import com.zayenha.qatra.analytics.infrastructure.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final AuditLogJpaRepository jpaRepository;
    private final AuditLogMapper mapper;

    @Override
    public AuditLog save(AuditLog log) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(log)));
    }

    @Override
    public PageResult<AuditLog> findAll(SearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = jpaRepository.findAllByOrderByTimestampDesc(pageable);
        return new PageResult<>(
            page.getContent().stream().map(mapper::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public List<AuditLog> findByAction(String action) {
        return jpaRepository.findByActionOrderByTimestampDesc(action).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return jpaRepository.findByUser_IdOrderByTimestampDesc(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByTimestampBetween(Instant from, Instant to) {
        return jpaRepository.findByTimestampBetweenOrderByTimestampDesc(from, to).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByAction(String action) {
        return jpaRepository.countByAction(action);
    }
}
