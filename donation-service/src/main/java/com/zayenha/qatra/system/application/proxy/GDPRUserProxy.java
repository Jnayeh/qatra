package com.zayenha.qatra.system.application.proxy;

import com.zayenha.qatra.user.api.UserApi;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import com.zayenha.qatra.user.infrastructure.web.dto.response.UserDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GDPRUserProxy {

    private final UserApi userApi;

    public UserDetailResponse getUser(Long id) {
        return userApi.getUser(id);
    }
    public void requestDeletion(Long id) { userApi.requestDeletion(id); }
}
