package com.zayenha.qatra.user.infrastructure.api;

import com.zayenha.qatra.user.api.UserApi;
import com.zayenha.qatra.user.api.dto.UserSummary;
import com.zayenha.qatra.user.application.mapper.UserDomainMapper;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserApiAdapter implements UserApi {

    private final UserQueryUseCases queryUseCases;

    @Override
    public Optional<UserSummary> findById(Long id) {
        return queryUseCases.findById(id).map(UserDomainMapper::toSummary);
    }

    @Override
    public Optional<UserSummary> findByEmail(String email) {
        return queryUseCases.findByEmail(email).map(UserDomainMapper::toSummary);
    }

    @Override
    public Optional<UserSummary> findByPhone(String phone) {
        return queryUseCases.findByPhone(phone).map(UserDomainMapper::toSummary);
    }

    @Override
    public boolean existsByEmail(String email) {
        return queryUseCases.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return queryUseCases.existsByPhone(phone);
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        return queryUseCases.getUserRoles(userId);
    }
}
