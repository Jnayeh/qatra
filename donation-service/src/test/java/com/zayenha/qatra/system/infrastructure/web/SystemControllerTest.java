package com.zayenha.qatra.system.infrastructure.web;

import com.zayenha.qatra.system.application.FeatureFlagService;
import com.zayenha.qatra.system.application.GDPRService;
import com.zayenha.qatra.system.application.SystemConfigService;
import com.zayenha.qatra.system.domain.model.FeatureFlag;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.model.SystemConfig;
import com.zayenha.qatra.system.infrastructure.web.dto.request.CreateFeatureFlagRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.GDPRActionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.GDPRRequestDeletionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.SetConfigRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    @Mock
    private SystemConfigService configService;
    @Mock
    private FeatureFlagService flagService;
    @Mock
    private GDPRService gdprService;

    private SystemController controller;

    @BeforeEach
    void setUp() {
        controller = new SystemController(configService, flagService, gdprService);
    }

    @Test
    void getConfigReturnsList() {
        var config = new SystemConfig("key1", "val1", "desc");
        when(configService.getAll()).thenReturn(List.of(config));

        var response = controller.getAllConfig();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void setConfigReturnsOk() {
        var config = new SystemConfig("key1", "val1", "desc");
        when(configService.set("key1", "val1", "desc")).thenReturn(config);

        var request = new SetConfigRequest("key1", "val1", "desc");
        var response = controller.setConfig(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().key()).isEqualTo("key1");
    }

    @Test
    void createFlagReturnsOk() {
        var flag = new FeatureFlag("test.flag", true, "Test");
        when(flagService.create("test.flag", true, "Test")).thenReturn(flag);

        var request = new CreateFeatureFlagRequest("test.flag", true, "Test");
        var response = controller.createFlag(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().flagName()).isEqualTo("test.flag");
    }

    @Test
    void requestDeletionReturnsOk() {
        var gdpr = new GDPRDeletionRequest(1L, "Reason");
        when(gdprService.requestDeletion(1L, "Reason")).thenReturn(gdpr);

        var request = new GDPRRequestDeletionRequest(1L, "Reason");
        var response = controller.requestDeletion(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().userId()).isEqualTo(1L);
    }
}
