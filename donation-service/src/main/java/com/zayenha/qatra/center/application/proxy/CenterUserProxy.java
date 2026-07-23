package com.zayenha.qatra.center.application.proxy;

import com.zayenha.qatra.user.api.UserApi;
import com.zayenha.qatra._shared.domain.Role;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CenterUserProxy {

    private final UserApi userApi;

    public boolean existsByUserIdAndRole(Long id, Role role) {
        return userApi.existsByUserIdAndRole(id, role);
    }
    public UserEntity getUserReference(Long id) {
        return userApi.getUserReference(id);
    }
}
