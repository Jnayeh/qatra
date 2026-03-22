package com.zayenha.qatra.system.application;

import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.system.domain.model.FeatureFlag;
import com.zayenha.qatra.system.domain.port.out.FeatureFlagRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagRepositoryPort repository;

    private FeatureFlagService service;

    @BeforeEach
    void setUp() {
        service = new FeatureFlagService(repository);
    }

    @Test
    void createReturnsSavedFlag() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.create("test.flag", true, "Test flag");

        assertThat(result.getFlagName()).isEqualTo("test.flag");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void enableUpdatesFlag() {
        var flag = new FeatureFlag("test.flag", false, "Test");
        when(repository.findByFlagName("test.flag")).thenReturn(Optional.of(flag));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.enable("test.flag");

        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void enableThrowsWhenNotFound() {
        when(repository.findByFlagName("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enable("nonexistent"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void isEnabledReturnsTrue() {
        when(repository.isEnabled("test.flag")).thenReturn(true);

        assertThat(service.isEnabled("test.flag")).isTrue();
    }
}
