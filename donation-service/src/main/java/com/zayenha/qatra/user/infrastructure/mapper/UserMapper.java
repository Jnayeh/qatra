package com.zayenha.qatra.user.infrastructure.mapper;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import com.zayenha.qatra.user.infrastructure.web.dto.response.UserDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserDetailResponse toDetail(User user, List<Role> roles);
    @Mapping(target = "roles", ignore = true)
    User toDomain(UserEntity entity);

    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(User domain);
}
