package com.zayenha.qatra.user.api;

import com.zayenha.qatra._shared.infrastructure.EntityApi;
import com.zayenha.qatra._shared.domain.Role;
import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import com.zayenha.qatra.user.infrastructure.mapper.UserMapper;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import com.zayenha.qatra.user.infrastructure.web.dto.response.UserDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserApi implements EntityApi<UserEntity> {

    private final UserJpaRepository userJpaRepository;
    private final UserRoleRepositoryPort userRoleRepository;
    private final UserCommandUseCases userCommand;
    private final UserQueryUseCases userQuery;
    private final UserMapper mapper;

    public UserEntity getUserReference(Long id) {
        return userJpaRepository.getReferenceById(id);
    }
    public UserDetailResponse getUser(Long id) {
        var user = userQuery.findById(id);
        return mapper.toDetail(user);
    }
    public boolean existsByUserIdAndRole(Long id, Role role) {
        return userRoleRepository.existsByUserIdAndRole(id, role);
    }
    public void requestDeletion(Long id) {
        userCommand.requestDeletion(id);
    }

    @Override
    public UserEntity getReference(Long id) {
        return getUserReference(id);
    }
}
