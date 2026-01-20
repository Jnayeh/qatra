package com.zayenha.qatra.user.api;

import com.zayenha.qatra.user.api.dto.UserSummary;
import com.zayenha.qatra.user.domain.model.Role;
import java.util.List;
import java.util.Optional;

public interface UserApi {
    Optional<UserSummary> findById(Long id);
    Optional<UserSummary> findByEmail(String email);
    Optional<UserSummary> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<Role> getUserRoles(Long userId);
}
