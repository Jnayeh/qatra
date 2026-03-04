package com.zayenha.qatra.appointment.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.appointment.domain.model.*;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.HealthScreeningEntity;
import com.zayenha.qatra.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.zayenha.qatra.appointment.infrastructure.persistence.repository.HealthScreeningJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {

    private final AppointmentJpaRepository jpaRepository;
    private final HealthScreeningJpaRepository screeningJpaRepository;

    @Override
    public Appointment save(Appointment appointment) {
        var entity = toEntity(appointment);
        if (entity.getId() != null) {
            var existing = jpaRepository.findById(entity.getId()).orElseThrow();
            existing.setStatus(entity.getStatus());
            existing.setCheckInTime(entity.getCheckInTime());
            existing.setCompletedAt(entity.getCompletedAt());
            existing.setOutcome(entity.getOutcome());
            existing.setNotes(entity.getNotes());
            existing.setUpdatedAt(entity.getUpdatedAt());
            return toDomain(jpaRepository.save(existing));
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Appointment> findByDonorId(Long donorId) {
        return jpaRepository.findByDonorIdOrderByCreatedAtDesc(donorId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByCenterIdAndDate(Long centerId, LocalDate date) {
        return jpaRepository.findByCenterIdAndAppointmentDateOrderByStartTime(centerId, date)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<Appointment> findAll(SearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = jpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public boolean existsByDonorIdAndStatusIn(Long donorId, List<AppointmentStatus> statuses) {
        return jpaRepository.existsByDonorIdAndStatusIn(donorId, statuses);
    }

    @Override
    public HealthScreening saveScreening(HealthScreening screening) {
        var entity = toScreeningEntity(screening);
        return toScreeningDomain(screeningJpaRepository.save(entity));
    }

    @Override
    public Optional<HealthScreening> findScreeningByAppointmentId(Long appointmentId) {
        return screeningJpaRepository.findByAppointmentId(appointmentId).map(this::toScreeningDomain);
    }

    private AppointmentEntity toEntity(Appointment domain) {
        var entity = new AppointmentEntity();
        entity.setId(domain.getId());
        entity.setDonorId(domain.getDonorId());
        entity.setSlotId(domain.getSlotId());
        entity.setCenterId(domain.getCenterId());
        entity.setStatus(domain.getStatus());
        entity.setAppointmentDate(domain.getAppointmentDate());
        entity.setStartTime(domain.getStartTime());
        entity.setEndTime(domain.getEndTime());
        entity.setCheckInTime(domain.getCheckInTime());
        entity.setCompletedAt(domain.getCompletedAt());
        entity.setOutcome(domain.getOutcome());
        entity.setNotes(domain.getNotes());
        return entity;
    }

    private Appointment toDomain(AppointmentEntity entity) {
        var domain = new Appointment();
        domain.setId(entity.getId());
        domain.setDonorId(entity.getDonorId());
        domain.setSlotId(entity.getSlotId());
        domain.setCenterId(entity.getCenterId());
        domain.setStatus(entity.getStatus());
        domain.setAppointmentDate(entity.getAppointmentDate());
        domain.setStartTime(entity.getStartTime());
        domain.setEndTime(entity.getEndTime());
        domain.setCheckInTime(entity.getCheckInTime());
        domain.setCompletedAt(entity.getCompletedAt());
        domain.setOutcome(entity.getOutcome());
        domain.setNotes(entity.getNotes());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    private HealthScreeningEntity toScreeningEntity(HealthScreening domain) {
        var entity = new HealthScreeningEntity();
        entity.setId(domain.getId());
        entity.setAppointmentId(domain.getAppointmentId());
        entity.setWeight(domain.getWeight());
        entity.setBloodPressure(domain.getBloodPressure());
        entity.setHemoglobin(domain.getHemoglobin());
        entity.setTemperature(domain.getTemperature());
        entity.setEligible(domain.getEligible());
        entity.setNotes(domain.getNotes());
        return entity;
    }

    private HealthScreening toScreeningDomain(HealthScreeningEntity entity) {
        var domain = new HealthScreening();
        domain.setId(entity.getId());
        domain.setAppointmentId(entity.getAppointmentId());
        domain.setWeight(entity.getWeight());
        domain.setBloodPressure(entity.getBloodPressure());
        domain.setHemoglobin(entity.getHemoglobin());
        domain.setTemperature(entity.getTemperature());
        domain.setEligible(entity.getEligible());
        domain.setNotes(entity.getNotes());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
