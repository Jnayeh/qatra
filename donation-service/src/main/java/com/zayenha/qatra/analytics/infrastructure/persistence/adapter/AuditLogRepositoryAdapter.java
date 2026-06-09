package com.zayenha.qatra.analytics.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import com.zayenha.qatra.analytics.infrastructure.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final AuditLogJpaRepository jpaRepository;
    private final AuditLogMapper mapper;
    private final CacheService cacheService;

    @Override
    public AuditLog save(AuditLog log) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(log)));
    }

    @Override
    public PageResult<AuditLog> findAll(SearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = jpaRepository.findAllByOrderByTimestampDesc(pageable);
        var total = cachedCount("count:auditLogs");
        return new PageResult<>(
            page.getContent().stream().map(mapper::toDomain).toList(),
            page.getNumber(), page.getSize(),
            total, page.getTotalPages()
        );
    }

    @Override
    public PageResult<AuditLog> findFiltered(SearchCriteria criteria, String action, Instant fromDate, Instant toDate) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = jpaRepository.findFiltered(action, fromDate, toDate, pageable);
        var total = jpaRepository.countFiltered(action, fromDate, toDate);
        return new PageResult<>(
            page.getContent().stream().map(mapper::toDomain).toList(),
            page.getNumber(), page.getSize(),
            total, page.getTotalPages()
        );
    }

    private long cachedCount(String key) {
        var cached = cacheService.get(key, Long.class);
        if (cached.isPresent()) return cached.get();
        var count = jpaRepository.count();
        cacheService.put(key, count, Duration.ofSeconds(6800));
        return count;
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

    @Override
    public long countByActionAndTimestampBetween(String action, Instant from, Instant to) {
        return jpaRepository.countByActionAndTimestampBetween(action, from, to);
    }
}
