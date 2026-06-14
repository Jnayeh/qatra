package com.zayenha.qatra.appointment.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditUtils;
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

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {

    private final AppointmentJpaRepository jpaRepository;
    private final HealthScreeningJpaRepository screeningJpaRepository;
    private final AppointmentMapper mapper;
    private final CacheService cacheService;

    @Override
    public Appointment save(Appointment appointment) {
        if (appointment.getId() != null) {
            var existing = jpaRepository.findById(appointment.getId()).orElseThrow();
            merge(existing, appointment);
            return mapper.toDomain(jpaRepository.save(existing));
        }
        appointment.setUserId(AuditUtils.currentUserId());
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
        return jpaRepository.findByCenterIdAndSlotDate(centerId, date)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<Appointment> findByCenterIdAndDateRange(Long centerId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = jpaRepository.findByCenterIdAndSlotDateBetween(centerId, fromDate, toDate, pageable);
        var total = cachedCount("count:appointments:by-center:" + centerId+"from:"+fromDate.toString()+"_to:"+toDate.toString());
        return new PageResult<>(
            result.getContent().stream().map(mapper::toDomain).toList(),
            result.getNumber(), result.getSize(),
            total, result.getTotalPages()
        );
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
        var total = cachedCount("count:appointments");
        return new PageResult<>(
            page.getContent().stream().map(mapper::toDomain).toList(),
            page.getNumber(), page.getSize(),
            total, page.getTotalPages()
        );
    }

    private long cachedCount(String key) {
        var cached = cacheService.get(key, Long.class);
        if (cached.isPresent()) return cached.get();
        var count = jpaRepository.count();
        cacheService.put(key, count, Duration.ofSeconds(6800));
        return count;
    }

    @Override
    public List<Appointment> findScheduledAppointmentsByDate(LocalDate targetDate) {
        return jpaRepository.findScheduledAppointmentsBySlotDate(targetDate).stream()
                .map(mapper::toDomain)
                .toList();
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
