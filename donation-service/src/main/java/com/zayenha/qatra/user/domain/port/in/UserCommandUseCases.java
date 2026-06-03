package com.zayenha.qatra.user.domain.port.in;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserStatus;

public interface UserCommandUseCases {
    User create(String email, String phone, String password, String displayName, String firstName, String lastName);
    User update(Long id, String email, String phone, String displayName);
    void updateStatus(Long id, UserStatus status, Long actorID);
    void assignRole(Long userId, Role role, Long actorID);
    void revokeRole(Long userId, Role role);
    void updatePassword(Long userId, String newEncodedPassword);
    void requestDeletion(Long userId);
    void delete(Long id);
    void verifyEmail(Long userId);
    void seedSuperAdminIfAbsent();
}
