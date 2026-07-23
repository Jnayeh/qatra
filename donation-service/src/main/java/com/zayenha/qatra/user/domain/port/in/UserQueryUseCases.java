package com.zayenha.qatra.user.domain.port.in;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.domain.Role;
import com.zayenha.qatra.user.domain.model.User;

import java.util.List;

public interface UserQueryUseCases {
    User findById(Long id);
    User findByEmail(String email);
    User findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<Role> getUserRoles(Long userId);
    PageResult<User> findAll(SearchCriteria criteria);
}
