package com.zayenha.qatra.user.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import com.zayenha.qatra.user.infrastructure.mapper.UserMapper;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;
    private final CacheService cacheService;

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(e -> toDomain(e, true));
    }
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(e -> toDomain(e, true));
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(e -> toDomain(e, true));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
    @Override
    public boolean existsOtherByEmailOrPhone(Long id, String email, String phone) {
        return jpaRepository.existsByIdNotAndEmailOrPhone(id, email, phone);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return jpaRepository.existsByPhone(phone);
    }

    @Override
    public PageResult<User> findAll(SearchCriteria criteria) {
        var spec = buildSpecification(criteria.search());
        var sort = buildSort(criteria.sortBy(), criteria.sortDirection());
        var pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        var page = jpaRepository.findAll(spec, pageable);
        var count = getTotalCount();
        return new PageResult<>(
            page.getContent().stream().map(e -> toDomain(e, true)).toList(),
            page.getNumber(),
            page.getSize(),
            count,
            page.getTotalPages()
        );
    }

    private long getTotalCount() {
        var cached = cacheService.get("count:users", Long.class);
        if (cached.isPresent()) return cached.get();
        var count = jpaRepository.count();
        cacheService.put("count:users", count, Duration.ofSeconds(6800));
        return count;
    }

    @Override
    public User save(User user) {
        var entity = toJpa(user);
        var saved = jpaRepository.save(entity);
        return toDomain(saved, false);
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

    private User toDomain(UserEntity e, boolean withRoles) {
        var u = mapper.toDomain(e);
        if (withRoles && e.getRoles() != null) {
            u.setRoles(e.getRoles().stream()
                    .map(UserRoleEntity::getRole).toList());
        }
        return u;
    }

    private UserEntity toJpa(User u) {
        var e = mapper.toEntity(u);
        if (u.getId() != null) e.setId(u.getId());
        return e;
    }
}
