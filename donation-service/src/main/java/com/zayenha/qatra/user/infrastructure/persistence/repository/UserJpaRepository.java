package com.zayenha.qatra.user.infrastructure.persistence.repository;

import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long>,
                                           JpaSpecificationExecutor<UserEntity> {
    @EntityGraph("UserEntity.withRoles")
    Page<UserEntity> findAll(Specification<UserEntity> spec, Pageable pageable);
    @EntityGraph("UserEntity.withRoles")
    Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByIdNotAndEmailOrPhone(Long id, String email, String phone);
}
