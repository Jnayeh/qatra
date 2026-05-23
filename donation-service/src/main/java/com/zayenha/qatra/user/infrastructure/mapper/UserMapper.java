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

    UserDetailResponse toDetail(User user);
    @Mapping(target = "roles", ignore = true)
    User toDomain(UserEntity entity);

    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(User domain);
}
