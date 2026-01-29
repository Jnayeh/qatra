package com.zayenha.qatra.user.domain.service;

import com.zayenha.qatra.user.domain.exception.AlreadyExistsException;
import com.zayenha.qatra.user.domain.exception.EmailAlreadyExistsException;
import com.zayenha.qatra.user.domain.exception.PhoneAlreadyExistsException;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDomainValidatorTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private UserDomainValidator validator;

    @Test
    void validateCreatePassesWhenEmailAndPhoneAreUnique() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);

        assertThatCode(() -> validator.validateCreate("new@example.com", "1234567890"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreateThrowsWhenEmailExists() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateCreate("existing@example.com", "1234567890"))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");
    }

    @Test
    void validateCreateThrowsWhenPhoneExists() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateCreate("new@example.com", "1234567890"))
                .isInstanceOf(PhoneAlreadyExistsException.class)
                .hasMessageContaining("1234567890");
    }

    @Test
    void validateCreateSkipsPhoneCheckWhenPhoneIsNull() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertThatCode(() -> validator.validateCreate("new@example.com", null))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdatePassesWhenNoConflict() {
        when(userRepository.existsOtherByEmailOrPhone(1L, "new@example.com", "1234567890")).thenReturn(false);

        assertThatCode(() -> validator.validateUpdate(1L, "new@example.com", "1234567890"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdateThrowsWhenOtherUserHasSameEmailOrPhone() {
        when(userRepository.existsOtherByEmailOrPhone(1L, "other@example.com", "1234567890")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateUpdate(1L, "other@example.com", "1234567890"))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("email or the phone number");
    }
}
