package com.zayenha.qatra.appointment.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.AppointmentType;
import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.appointment.application.proxy.AptCenterProxy;
import com.zayenha.qatra.appointment.application.proxy.AptDonorProxy;
import com.zayenha.qatra.appointment.domain.model.*;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import com.zayenha.qatra.center.application.api.dto.SlotDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepositoryPort repository;
    @Mock
    private AptCenterProxy centerProxy;
    @Mock
    private AptDonorProxy donorProxy;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EventPublisherPort eventPublisherPort;
    @Mock
    private CacheService cacheService;
    @Mock
    private AuditPublisher auditPublisher;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(repository, centerProxy, donorProxy, eventPublisher, eventPublisherPort, cacheService, auditPublisher);
    }

    @Test
    void bookCreatesAppointment() {
        var slot = new SlotDTO(100L, 1000L, 1, 0, 10, 5, false);
        when(centerProxy.findSlotById(100L)).thenReturn(Optional.of(slot));
        when(repository.existsByDonorIdAndStatusIn(1L, List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_SCREENING))).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.book(1L, 100L, null, AppointmentType.REGULAR);

        assertThat(result.getDonorId()).isEqualTo(1L);
        assertThat(result.getSlotId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(repository).save(any());
    }

    @Test
    void bookThrowsWhenDonorHasActiveAppointment() {
        when(repository.existsByDonorIdAndStatusIn(1L, List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_SCREENING))).thenReturn(true);

        assertThatThrownBy(() -> service.book(1L, 100L, null, AppointmentType.REGULAR))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void checkInUpdatesStatus() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.checkIn(1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN);
        assertThat(result.getCheckedInAt()).isNotNull();
    }

    @Test
    void checkInThrowsWhenNotScheduled() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.checkIn(1L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void completeSetsOutcome() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(1L);
        appointment.setMlCollected(450);
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.complete(1L, DonationOutcome.COMPLETED, "Good");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(result.getOutcome()).isEqualTo(DonationOutcome.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void cancelChangesStatus() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.cancel(1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelThrowsWhenCompleted() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void cancelByDonorValidatesOwnership() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.cancelByDonor(1L, 1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelByDonorThrowsWhenNotOwner() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancelByDonor(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByIdReturnsAppointment() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));

        var result = service.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.findById(99L)).isEmpty();
    }

    @Test
    void saveScreeningPersistsAndReturns() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(10L);
        when(repository.findById(1L)).thenReturn(Optional.of(appointment));
        var screening = new HealthScreening();
        screening.setId(1L);
        screening.setAppointmentId(1L);
        screening.setEligible(true);
        when(repository.saveScreening(any())).thenReturn(screening);

        var result = service.saveScreening(1L, 70.0, "120/80", 14.5, 36.6, true, "Fit");

        assertThat(result.getAppointmentId()).isEqualTo(1L);
        assertThat(result.getEligible()).isTrue();
        verify(repository).saveScreening(any());
    }
}
