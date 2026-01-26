package com.zayenha.qatra.user.infrastructure.persistence.adapter;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.UserRole;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserRoleEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRoleRepositoryAdapter implements UserRoleRepositoryPort {
    private final UserRoleJpaRepository jpaRepository;

    @Override
    public List<UserRole> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(e -> toDomain(e, userId)).toList();
    }

    @Override
    public boolean existsByUserIdAndRole(Long userId, Role role) {
        return jpaRepository.existsByUserIdAndRole(userId, role);
    }

    @Override
    public UserRole save(UserRole userRole) {
        var entity = toJpa(userRole);
        var saved = jpaRepository.save(entity);
        return toDomain(saved, userRole.getUserId());
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteByUserIdAndRole(Long userId, Role role) {
        jpaRepository.deleteByUserIdAndRole(userId, role);
    }

    private UserRole toDomain(UserRoleEntity e, Long userId) {
        return UserRole.reconstruct(e.getId(), userId, e.getRole(), e.getAssignedAt());
    }

    private UserRoleEntity toJpa(UserRole r) {
        var e = new UserRoleEntity();
        if (r.getId() != null) e.setId(r.getId());
        e.setUser(new UserEntity(r.getUserId()));
        e.setRole(r.getRole());
        e.setAssignedAt(r.getAssignedAt());
        return e;
    }
}
