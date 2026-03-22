package com.zayenha.qatra.system.application;

import com.zayenha.qatra.system.domain.model.SystemConfig;
import com.zayenha.qatra.system.domain.port.out.SystemConfigRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepositoryPort repository;

    private SystemConfigService service;

    @BeforeEach
    void setUp() {
        service = new SystemConfigService(repository);
    }

    @Test
    void setCreatesNewConfig() {
        when(repository.findByKey("test.key")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.set("test.key", "test.value", "Test description");

        assertThat(result.getConfigKey()).isEqualTo("test.key");
        assertThat(result.getConfigValue()).isEqualTo("test.value");
    }

    @Test
    void setUpdatesExistingConfig() {
        var existing = new SystemConfig("test.key", "old.value", "Old");
        when(repository.findByKey("test.key")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.set("test.key", "new.value", "Updated");

        assertThat(result.getConfigValue()).isEqualTo("new.value");
    }
}
