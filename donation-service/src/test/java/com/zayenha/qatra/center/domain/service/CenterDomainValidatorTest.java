package com.zayenha.qatra.center.domain.service;

import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CenterDomainValidatorTest {

    @Mock
    private CenterRepositoryPort centerRepository;

    @InjectMocks
    private CenterDomainValidator validator;

    @Test
    void validateCreatePassesWhenNameIsUnique() {
        when(centerRepository.existsByName("Unique")).thenReturn(false);

        assertThatCode(() -> validator.validateCreate("Unique"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreateThrowsWhenNameExists() {
        when(centerRepository.existsByName("Dup")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateCreate("Dup"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Dup");
    }

    @Test
    void validateUpdatePassesWhenNoConflict() {
        when(centerRepository.otherCenterHasName(1L, "Unique")).thenReturn(false);

        assertThatCode(() -> validator.validateUpdate(1L, "Unique"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdateThrowsWhenOtherCenterHasSameName() {
        when(centerRepository.otherCenterHasName(1L, "Dup")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateUpdate(1L, "Dup"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Dup");
    }
}
