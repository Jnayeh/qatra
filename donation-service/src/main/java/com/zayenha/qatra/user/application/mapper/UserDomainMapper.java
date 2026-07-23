package com.zayenha.qatra.user.application.mapper;

import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra._shared.domain.UserStatus;

public class UserDomainMapper {

    public record UserSummary(Long id, String email, String phone, String displayName, UserStatus status) {}

    public static UserSummary toSummary(User user) {
        return new UserSummary(
            user.getId(),
            user.getEmail(),
            user.getPhone(),
            user.getDisplayName(),
            user.getStatus()
        );
    }
}
