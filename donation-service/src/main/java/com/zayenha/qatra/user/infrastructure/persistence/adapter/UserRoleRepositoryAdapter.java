package com.zayenha.qatra.user.infrastructure.persistence.adapter;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.UserRole;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserRoleEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRoleRepositoryAdapter implements UserRoleRepositoryPort {
    private final UserRoleJpaRepository jpaRepository;

    @Override
    public List<UserRole> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<UserRole> findByUserIdAndRole(Long userId, Role role) {
        return jpaRepository.findByUserIdAndRole(userId, role).map(this::toDomain);
    }

    @Override
    public UserRole save(UserRole userRole) {
        var entity = toJpa(userRole);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteByUserIdAndRole(Long userId, Role role) {
        jpaRepository.deleteByUserIdAndRole(userId, role);
    }

    private UserRole toDomain(UserRoleEntity e) {
        return UserRole.reconstruct(e.getId(), e.getUserId(), e.getRole(), e.getAssignedAt());
    }

    private UserRoleEntity toJpa(UserRole r) {
        var e = new UserRoleEntity();
        if (r.getId() != null) e.setId(r.getId());
        e.setUserId(r.getUserId());
        e.setRole(r.getRole());
        e.setAssignedAt(r.getAssignedAt());
        return e;
    }
}
