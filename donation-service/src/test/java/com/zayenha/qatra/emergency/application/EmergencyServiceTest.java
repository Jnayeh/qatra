package com.zayenha.qatra.emergency.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import com.zayenha.qatra._shared.domain.port.out.AppointmentServiceProvider;
import com.zayenha.qatra.emergency.application.proxy.EmergencyCenterProxy;
import com.zayenha.qatra.emergency.application.proxy.EmergencyDonorProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zayenha.qatra.donor.application.api.dto.DonorProfileDTO;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock
    private AppointmentServiceProvider appointmentApi;
    @Mock
    private EmergencyRepositoryPort repository;
    @Mock
    private EmergencyDonorProxy donorProxy;
    @Mock
    private EmergencyCenterProxy centerProxy;
    @Mock
    private CacheService cacheService;
    @Mock
    private MatchingService matchingService;
    @Mock
    private AuditPublisher auditPublisher;

    private EmergencyService service;

    @BeforeEach
    void setUp() {
        service = new EmergencyService(appointmentApi, repository, donorProxy, centerProxy, cacheService, matchingService, auditPublisher);
    }

    @Test
    void createReturnsSavedRequest() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.create(1L, 10L, BloodType.A_POSITIVE, 2,
                EmergencyUrgency.HIGH, 50, "+123456789");

        assertThat(result.getCenterId()).isEqualTo(1L);
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

        var result = service.update(1L, 2L, BloodType.B_POSITIVE, 3,
                EmergencyUrgency.CRITICAL, 100, "+987654321");

        assertThat(result.getBloodType()).isEqualTo(BloodType.B_POSITIVE);
        assertThat(result.getUnitsNeeded()).isEqualTo(3);
    }

    @Test
    void updateThrowsWhenNotOpen() {
        var existing = new EmergencyRequest();
        existing.setId(1L);
        existing.setStatus(EmergencyStatus.FULFILLED);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(1L, 2L, BloodType.A_POSITIVE, 1,
                EmergencyUrgency.LOW, 10, ""))
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
    void acceptResponseUpdatesAndFulfillsEmergency() {
        when(repository.saveResponse(any())).thenAnswer(i -> i.getArgument(0));

        var emergency = new EmergencyRequest();
        emergency.setId(1L);
        emergency.setUnitsNeeded(2);
        emergency.setStatus(EmergencyStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(emergency));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var donor = new DonorProfileDTO();
        donor.setConsecutiveEmergencyDeclines(0);
        when(donorProxy.findByUserId(10L)).thenReturn(donor);
        var accepted = new DonorResponse(1L, 99L);
        accepted.accept(200L);
        when(repository.findResponsesByEmergencyId(1L)).thenReturn(List.of(accepted));

        var result = service.acceptResponse(1L, 10L, 100L);

        assertThat(result.getStatus()).isEqualTo(ResponseStatus.ACCEPTED);
        assertThat(result.getSlotId()).isEqualTo(100L);
        verify(repository).save(any());
        verify(repository).saveResponse(any());
    }

    @Test
    void declineResponseUpdatesStatus() {
        var emergency = new EmergencyRequest();
        emergency.setId(1L);
        emergency.setUnitsNeeded(2);
        emergency.setStatus(EmergencyStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(emergency));
        when(repository.saveResponse(any())).thenAnswer(i -> i.getArgument(0));

        var donor = new DonorProfileDTO();
        donor.setConsecutiveEmergencyDeclines(0);
        when(donorProxy.findByUserId(10L)).thenReturn(donor);

        var result = service.declineResponse(1L, 10L, "not available");

        assertThat(result.getStatus()).isEqualTo(ResponseStatus.DECLINED);
        assertThat(result.getRespondedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsEmergency() {
        var request = new EmergencyRequest();
        request.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(request));

        assertThat(service.findById(1L)).isEqualTo(request);
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(com.zayenha.qatra._shared.exception.NotFoundException.class);
    }
}
