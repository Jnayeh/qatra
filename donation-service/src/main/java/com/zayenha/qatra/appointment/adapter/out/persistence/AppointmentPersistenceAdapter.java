package com.zayenha.qatra.appointment.adapter.out.persistence;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppointmentPersistenceAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpaRepository;

    @Override
    public Optional<Appointment> findById(AppointmentId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Appointment> findByDonorId(Long donorId) {
        return jpaRepository.findByDonorIdOrderByCreatedAtDesc(donorId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByCenterIdAndDate(Long centerId, LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant();
        return jpaRepository.findByCenterIdAndDateRange(centerId, start, end)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentJpaEntity entity = toJpa(appointment);
        AppointmentJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void delete(AppointmentId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public long countByStatus(AppointmentStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public long totalMlCollected() {
        return jpaRepository.totalMlCollected();
    }

    @Override
    public long totalMlCollectedBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.totalMlCollectedBetween(toInstant(start), toInstant(end));
    }

    @Override
    public long countCompletedBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.countCompletedBetween(toInstant(start), toInstant(end));
    }

    @Override
    public long countByCenterIdAndStatus(Long centerId, AppointmentStatus status) {
        return jpaRepository.countByCenterIdAndStatus(centerId, status);
    }

    @Override
    public List<Object[]> donationSummaryByCenter() {
        return jpaRepository.donationSummaryByCenter();
    }

    @Override
    public List<Object[]> dailyDonationStats(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.dailyDonationStats(toInstant(start), toInstant(end));
    }

    @Override
    public List<Appointment> findAll(Long centerId, LocalDate from, LocalDate to, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        Instant start = from != null ? from.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant end = to != null ? to.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant() : null;
        Page<AppointmentJpaEntity> result = jpaRepository.findAllFiltered(centerId, start, end, pageable);
        return result.stream().map(this::toDomain).toList();
    }

    @Override
    public long countAll(Long centerId, LocalDate from, LocalDate to) {
        Instant start = from != null ? from.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant end = to != null ? to.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant() : null;
        return jpaRepository.countAllFiltered(centerId, start, end);
    }

    private Appointment toDomain(AppointmentJpaEntity entity) {
        return Appointment.reconstruct(
                new AppointmentId(entity.getId()),
                entity.getDonorId(),
                entity.getCenterId(),
                entity.getSlotId(),
                entity.getAppointmentType(),
                entity.getEmergencyId(),
                entity.getStatus(),
                entity.getMlCollected(),
                entity.getNotes(),
                entity.getCancellationReason(),
                entity.getQrCode(),
                entity.getCompletedByStaffId(),
                entity.getCreatedAt(),
                entity.getConfirmedAt(),
                entity.getCompletedAt(),
                entity.getCancelledAt()
        );
    }

    private AppointmentJpaEntity toJpa(Appointment appointment) {
        AppointmentJpaEntity entity = new AppointmentJpaEntity();
        if (appointment.getId() != null && appointment.getId().value() != null) {
            entity.setId(appointment.getId().value());
        }
        entity.setDonorId(appointment.getDonorId());
        entity.setCenterId(appointment.getCenterId());
        entity.setEmergencyId(appointment.getEmergencyId());
        entity.setSlotId(appointment.getSlotId());
        entity.setStatus(appointment.getStatus());
        entity.setAppointmentType(appointment.getAppointmentType());
        entity.setMlCollected(appointment.getMlCollected());
        entity.setNotes(appointment.getNotes());
        entity.setCancellationReason(appointment.getCancellationReason());
        entity.setQrCode(appointment.getQrCode());
        entity.setCompletedByStaffId(appointment.getCompletedByStaffId());
        entity.setCreatedAt(appointment.getCreatedAt());
        entity.setConfirmedAt(appointment.getConfirmedAt());
        entity.setCompletedAt(appointment.getCompletedAt());
        entity.setCancelledAt(appointment.getCancelledAt());
        return entity;
    }

    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }
}
