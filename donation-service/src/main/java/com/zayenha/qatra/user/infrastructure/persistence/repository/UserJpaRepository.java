package com.zayenha.qatra.user.infrastructure.persistence.repository;

import com.zayenha.qatra._shared.domain.UserStatus;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long>,
                                           JpaSpecificationExecutor<UserEntity> {
    @EntityGraph("UserEntity.withRoles")
    Page<UserEntity> findAll(Specification<UserEntity> spec, Pageable pageable);
    @EntityGraph("UserEntity.withRoles")
    Optional<UserEntity> findById(Long id);
    @EntityGraph("UserEntity.withRoles")
    Optional<UserEntity> findByEmail(String email);
    @EntityGraph("UserEntity.withRoles")
    Optional<UserEntity> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    @Query("SELECT d.id FROM UserEntity d WHERE d.id <> :id AND (d.email = :email OR d.phone = :phone)")
    Boolean existsByNotIDAndEmailOrPhone(@Param("id") Long id,@Param("email")  String email,@Param("phone")  String phone);
    List<UserEntity> findByStatus(UserStatus status);
}
