package com.zayenha.qatra.user.domain.port.in;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserQueryUseCases {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<Role> getUserRoles(Long userId);
    PageResult<User> findAll(SearchCriteria criteria);
}
