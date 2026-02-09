package com.zayenha.qatra.user.domain.port.out;

import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.shared.domain.SearchCriteria;
import com.zayenha.qatra.user.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    boolean existsById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsOtherByEmailOrPhone(Long id, String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    PageResult<User> findAll(SearchCriteria criteria);
    User save(User user);
    void deleteById(Long id);
}
