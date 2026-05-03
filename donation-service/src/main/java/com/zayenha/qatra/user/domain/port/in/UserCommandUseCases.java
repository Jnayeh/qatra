package com.zayenha.qatra.user.domain.port.in;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserStatus;

public interface UserCommandUseCases {
    User create(String email, String phone, String password, String displayName);
    User update(Long id, String email, String phone, String displayName);
    void updateStatus(Long id, UserStatus status);
    void assignRole(Long userId, Role role);
    void revokeRole(Long userId, Role role);
    void updatePassword(Long userId, String newEncodedPassword);
    void delete(Long id);
    void seedSuperAdminIfAbsent();
}
