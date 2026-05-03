package com.zayenha.qatra.appointment.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import com.zayenha.qatra.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.zayenha.qatra.appointment.infrastructure.mapper.AppointmentMapper;
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
    private final AppointmentMapper mapper;

    @Override
    public Appointment save(Appointment appointment) {
        if (appointment.getId() != null) {
            var existing = jpaRepository.findById(appointment.getId()).orElseThrow();
            merge(existing, appointment);
            return mapper.toDomain(jpaRepository.save(existing));
        }
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(appointment)));
    }

    private void merge(AppointmentEntity existing, Appointment source) {
        var updated = mapper.toEntity(source);
        existing.setStatus(updated.getStatus());
        existing.setCheckedInAt(updated.getCheckedInAt());
        existing.setStartedAt(updated.getStartedAt());
        existing.setCompletedAt(updated.getCompletedAt());
        existing.setCancelledAt(updated.getCancelledAt());
        existing.setCancellationReason(updated.getCancellationReason());
        existing.setOutcome(updated.getOutcome());
        existing.setNotes(updated.getNotes());
        existing.setMlCollected(updated.getMlCollected());
        existing.setCompletedByStaff(updated.getCompletedByStaff());
        existing.setUpdatedAt(updated.getUpdatedAt());
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Appointment> findByDonorId(Long donorId) {
        return jpaRepository.findByDonor_IdOrderByCreatedAtDesc(donorId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Appointment> findByCenterIdAndDate(Long centerId, LocalDate date) {
        return jpaRepository.findByCenter_IdOrderByCreatedAtDesc(centerId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Appointment> findByEmergencyId(Long emergencyId) {
        return jpaRepository.findByEmergency_Id(emergencyId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countCompletedByEmergencyId(Long emergencyId) {
        return jpaRepository.countCompletedByEmergencyId(emergencyId, DonationOutcome.COMPLETED, AppointmentStatus.COMPLETED);
    }

    @Override
    public PageResult<Appointment> findAll(SearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = jpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        return new PageResult<>(
            page.getContent().stream().map(mapper::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public boolean existsByDonorIdAndStatusIn(Long donorId, List<AppointmentStatus> statuses) {
        return jpaRepository.existsByDonor_IdAndStatusIn(donorId, statuses);
    }

    @Override
    public HealthScreening saveScreening(HealthScreening screening) {
        var entity = mapper.toScreeningEntity(screening);
        return mapper.toScreeningDomain(screeningJpaRepository.save(entity));
    }

    @Override
    public Optional<HealthScreening> findScreeningByAppointmentId(Long appointmentId) {
        return screeningJpaRepository.findByAppointment_Id(appointmentId).map(mapper::toScreeningDomain);
    }
}
