package com.zayenha.qatra.user.infrastructure.persistence.repository;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUser_Id(Long userId);
    boolean existsByUser_IdAndRole(Long userId, Role role);
    void deleteByUser_Id(Long userId);
    void deleteByUser_IdAndRole(Long userId, Role role);
}
