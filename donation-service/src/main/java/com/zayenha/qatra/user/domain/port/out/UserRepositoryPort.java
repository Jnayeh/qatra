package com.zayenha.qatra.user.domain.port.out;

import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    boolean existsById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsOtherByEmailOrPhone(Long id, String email, String phone);
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
    List<User> findAll();
    PageResult<User> findAll(UserSearchCriteria criteria);
    User save(User user);
    void deleteById(Long id);
}
