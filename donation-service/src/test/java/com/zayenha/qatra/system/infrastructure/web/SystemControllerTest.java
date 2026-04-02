package com.zayenha.qatra.system.infrastructure.web;

import com.zayenha.qatra.system.application.GDPRService;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.GDPRActionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.GDPRRequestDeletionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    @Mock
    private GDPRService gdprService;

    private SystemController controller;

    @BeforeEach
    void setUp() {
        controller = new SystemController(gdprService);
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
