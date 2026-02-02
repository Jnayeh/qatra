package com.zayenha.qatra.user.infrastructure.web.mapper;

import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.infrastructure.web.dto.response.UserDetailResponse;
import java.util.List;

public class UserMapper {

    public static UserDetailResponse toDetail(User user, List<com.zayenha.qatra.user.domain.model.Role> roles) {
        return new UserDetailResponse(
            user.getId(),
            user.getEmail(),
            user.getPhone(),
            user.getDisplayName(),
            user.getStatus(),
            user.isEmailVerified(),
            roles,
            user.getCreatedAt(),
            user.getLastActiveAt()
        );
    }
}
