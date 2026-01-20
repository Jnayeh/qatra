package com.zayenha.qatra.user.application.mapper;

import com.zayenha.qatra.user.api.dto.UserSummary;
import com.zayenha.qatra.user.domain.model.User;

public class UserDomainMapper {

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
