package com.zayenha.qatra.user.infrastructure.persistence.adapter;

import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.user.domain.exception.UserNotFoundException;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserSearchCriteria;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import com.zayenha.qatra.user.infrastructure.persistence.adapter.utils.TimedCount;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserRoleEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository jpaRepository;
    private final Map<String, TimedCount> countCache = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return jpaRepository.existsByPhone(phone);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<User> findAll(UserSearchCriteria criteria) {
        var spec = buildSpecification(criteria.search());
        var sort = buildSort(criteria.sortBy(), criteria.sortDirection());
        var pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        var page = jpaRepository.findAll(spec, pageable);
        var count = getTotalCount();
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(),
            page.getSize(),
            count,
            page.getTotalPages()
        );
    }

    private long getTotalCount() {
        var countTime = Instant.now();
        var total = countCache.get("total");
        log.info("Cached value: {}", total);
        log.info("Current time: {}", countTime);
        if (total == null || total.ttl().isBefore(countTime)) {
            var count = jpaRepository.count();
            var ttl = countTime.plusSeconds(60);
            countCache.put("total", new TimedCount(count, ttl));
            return count;
        }
        return total.count();
    }

    @Override
    public User save(User user) {
        var entity = toJpa(user);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Specification<UserEntity> buildSpecification(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            var predicates = new ArrayList<Predicate>();
            var pattern = "%" + search.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("email")), pattern));
            predicates.add(cb.like(cb.lower(root.get("phone")), pattern));
            predicates.add(cb.like(cb.lower(root.get("displayName")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        var allowed = List.of("id", "email", "phone", "displayName", "status", "createdAt");
        var field = allowed.contains(sortBy) ? sortBy : "id";
        var dir = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field);
    }

    private User toDomain(UserEntity e) {
        var roles = e.getRoles().stream()
                .map(UserRoleEntity::getRole)
                .toList();
        return User.reconstruct(
            e.getId(), e.getEmail(), e.getPhone(),
            e.getHashedPassword(), e.getDisplayName(),
            e.getStatus(), e.isEmailVerified(),
            e.getDeletedAt(), e.getCreatedAt(), e.getLastActiveAt(),
            roles
        );
    }

    private UserEntity toJpa(User u) {
        var e = new UserEntity();
        if (u.getId() != null) e.setId(u.getId());
        e.setEmail(u.getEmail());
        e.setPhone(u.getPhone());
        e.setHashedPassword(u.getHashedPassword());
        e.setDisplayName(u.getDisplayName());
        e.setStatus(u.getStatus());
        e.setEmailVerified(u.isEmailVerified());
        e.setDeletedAt(u.getDeletedAt());
        e.setCreatedAt(u.getCreatedAt());
        e.setLastActiveAt(u.getLastActiveAt());
        return e;
    }
}
