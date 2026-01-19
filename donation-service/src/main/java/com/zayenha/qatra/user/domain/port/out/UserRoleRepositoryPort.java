package com.zayenha.qatra.user.domain.port.out;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserRoleRepositoryPort {
    List<UserRole> findByUserId(Long userId);
    Optional<UserRole> findByUserIdAndRole(Long userId, Role role);
    UserRole save(UserRole userRole);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndRole(Long userId, Role role);
}
