package com.zayenha.qatra.user.infrastructure.persistence.repository;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserId(Long userId);
    boolean existsByUserIdAndRole(Long userId, Role role);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndRole(Long userId, Role role);
}
