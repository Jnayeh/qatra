package com.zayenha.qatra.user.infrastructure.persistence.adapter;

import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserSearchCriteria;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository jpaRepository;

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

    @Override
    public PageResult<User> findAll(UserSearchCriteria criteria) {
        var spec = buildSpecification(criteria.search());
        var sort = buildSort(criteria.sortBy(), criteria.sortDirection());
        var pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        var page = jpaRepository.findAll(spec, pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
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
        return User.reconstruct(
            e.getId(), e.getEmail(), e.getPhone(),
            e.getHashedPassword(), e.getDisplayName(),
            e.getStatus(), e.isEmailVerified(),
            e.getDeletedAt(), e.getCreatedAt(), e.getLastActiveAt()
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
