package com.zayenha.qatra.emergency.application;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock
    private EmergencyRepositoryPort repository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EmergencyService service;

    @BeforeEach
    void setUp() {
        service = new EmergencyService(repository, eventPublisher);
    }

    @Test
    void createReturnsSavedRequest() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.create("John Doe", BloodType.A_POSITIVE, 2,
                EmergencyUrgency.HIGH, "General Hospital", 40.71, -74.00, "+123456789");

        assertThat(result.getPatientName()).isEqualTo("John Doe");
        assertThat(result.getBloodType()).isEqualTo(BloodType.A_POSITIVE);
        assertThat(result.getStatus()).isEqualTo(EmergencyStatus.OPEN);
        assertThat(result.getExpiresAt()).isNotNull();
        verify(repository).save(any());
    }

    @Test
    void updateModifiesOpenEmergency() {
        var existing = new EmergencyRequest();
        existing.setId(1L);
        existing.setStatus(EmergencyStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.update(1L, "Jane Doe", BloodType.B_POSITIVE, 3,
                EmergencyUrgency.CRITICAL, "City Hospital", 40.71, -74.00, "+987654321");

        assertThat(result.getPatientName()).isEqualTo("Jane Doe");
        assertThat(result.getBloodType()).isEqualTo(BloodType.B_POSITIVE);
    }

    @Test
    void updateThrowsWhenNotOpen() {
        var existing = new EmergencyRequest();
        existing.setId(1L);
        existing.setStatus(EmergencyStatus.FULFILLED);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(1L, "Jane", BloodType.A_POSITIVE, 1,
                EmergencyUrgency.LOW, "Hosp", 0.0, 0.0, ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void cancelChangesStatus() {
        var request = new EmergencyRequest();
        request.setId(1L);
        request.setStatus(EmergencyStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.cancel(1L);

        assertThat(result.getStatus()).isEqualTo(EmergencyStatus.CANCELLED);
    }

    @Test
    void cancelThrowsWhenFulfilled() {
        var request = new EmergencyRequest();
        request.setId(1L);
        request.setStatus(EmergencyStatus.FULFILLED);
        when(repository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void respondCreatesPendingResponse() {
        var request = new EmergencyRequest();
        request.setId(1L);
        request.setStatus(EmergencyStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.existsByEmergencyIdAndDonorId(1L, 10L)).thenReturn(false);
        when(repository.saveResponse(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.respond(1L, 10L);

        assertThat(result.getEmergencyId()).isEqualTo(1L);
        assertThat(result.getDonorId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(ResponseStatus.PENDING);
    }

    @Test
    void respondThrowsWhenAlreadyResponded() {
        var request = new EmergencyRequest();
        request.setId(1L);
        request.setStatus(EmergencyStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.existsByEmergencyIdAndDonorId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.respond(1L, 10L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void acceptResponseUpdatesAndFulfillsEmergency() {
        var response = new DonorResponse();
        response.setId(1L);
        response.setEmergencyId(1L);
        response.setDonorId(10L);
        response.setStatus(ResponseStatus.PENDING);
        when(repository.findResponseById(1L)).thenReturn(Optional.of(response));
        when(repository.saveResponse(any())).thenAnswer(i -> i.getArgument(0));

        var emergency = new EmergencyRequest();
        emergency.setId(1L);
        emergency.setStatus(EmergencyStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(emergency));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.acceptResponse(1L, 100L);

        assertThat(result.getStatus()).isEqualTo(ResponseStatus.ACCEPTED);
        assertThat(result.getSlotId()).isEqualTo(100L);
        verify(repository).save(any());
        verify(repository).saveResponse(any());
    }

    @Test
    void declineResponseUpdatesStatus() {
        var response = new DonorResponse();
        response.setId(1L);
        response.setStatus(ResponseStatus.PENDING);
        when(repository.findResponseById(1L)).thenReturn(Optional.of(response));
        when(repository.saveResponse(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.declineResponse(1L);

        assertThat(result.getStatus()).isEqualTo(ResponseStatus.DECLINED);
        assertThat(result.getRespondedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsEmergency() {
        var request = new EmergencyRequest();
        request.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(request));

        assertThat(service.findById(1L)).isPresent();
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.findById(99L)).isEmpty();
    }
}
