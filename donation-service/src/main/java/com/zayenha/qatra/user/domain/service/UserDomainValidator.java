package com.zayenha.qatra.user.domain.service;

import com.zayenha.qatra.shared.exception.ConflictException;
import com.zayenha.qatra.user.domain.exception.EmailAlreadyExistsException;
import com.zayenha.qatra.user.domain.exception.PhoneAlreadyExistsException;
import com.zayenha.qatra.user.domain.model.UserStatus;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;

public class UserDomainValidator {
    private final UserRepositoryPort userRepository;

    public UserDomainValidator(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public void validateCreate(String email, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        if (phone !=null && userRepository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException(phone);
        }
    }

    public void validateUpdate(Long userId, String email, String phone) {
        if (userRepository.existsOtherByEmailOrPhone(userId,email, phone)) {
                throw new ConflictException("The email or the phone number belong to another user", "USER_CONFLICT");
        }
    }
}
