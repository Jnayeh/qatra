package com.zayenha.qatra.center.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.center.domain.exception.CenterErrorCode;
import com.zayenha.qatra.center.domain.model.*;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases;
import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.domain.port.out.SlotRepositoryPort;
import com.zayenha.qatra.center.domain.service.CenterDomainValidator;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CenterService implements CenterCommandUseCases, CenterQueryUseCases {

    private final CenterRepositoryPort centerRepository;
    private final SlotRepositoryPort slotRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheService cacheService;
    private final AuditPublisher auditPublisher;
    private final UserRoleRepositoryPort userRoleRepository;

    private CenterDomainValidator validator() {
        return new CenterDomainValidator(centerRepository);
    }

    @Override
    @Transactional
    public DonationCenter create(CreateCenterCommand command) {
        validator().validateCreate(command.name());
        var userId = AuditUtils.currentUserId();
        var center = new DonationCenter(
            command.name(), command.address(), command.city(),
            command.country(), command.postalCode(), command.phone(),
            command.email(), command.latitude(), command.longitude(),
            command.facilityType(), command.operatingHours(),
            command.totalCapacity(), command.maxRegular(), command.slotPeriod(),
            userId
        );
        var saved = centerRepository.save(center);
        cacheService.evictByPattern("donationCenters:*");
        auditPublisher.publish("CENTER_CREATED", saved.getId(), "DonationCenter", null, Map.of(
            "name", command.name(), "city", command.city(),
            "facilityType", command.facilityType().name()));
        if (userId != 0 && userRoleRepository.existsByUserIdAndRole(userId, Role.CENTER_ADMIN)
                && centerRepository.findAdminByUserId(userId).isEmpty()) {
            centerRepository.saveAdmin(new CenterAdminProfile(userId, saved.getId()));
        }
        return saved;
    }

    @Override
    @Transactional
    public DonationCenter update(Long id, UpdateCenterCommand command) {
        validator().validateUpdate(id, command.name());
        var center = centerRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
        var oldName = center.getName();
        center.setName(command.name());
        center.setAddress(command.address());
        center.setCity(command.city());
        center.setCountry(command.country());
        center.setPostalCode(command.postalCode());
        center.setPhone(command.phone());
        center.setEmail(command.email());
        center.setLatitude(command.latitude());
        center.setLongitude(command.longitude());
        center.setFacilityType(command.facilityType());
        center.setOperatingHours(command.operatingHours());
        center.setTotalCapacity(command.totalCapacity());
        center.setMaxRegular(command.maxRegular());
        center.setSlotPeriod(command.slotPeriod());
        center.setUpdatedAt(java.time.Instant.now());
        var saved = centerRepository.save(center);
        cacheService.evictByPattern("donationCenters:*");
        auditPublisher.publish("CENTER_UPDATED", saved.getId(), "DonationCenter",
            Map.of("name", oldName),
            Map.of("name", command.name(), "city", command.city(),
                   "facilityType", command.facilityType().name()));
        return saved;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, CenterStatus status) {
        var center = centerRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
        var oldStatus = center.getStatus();
        center.setStatus(status);
        centerRepository.save(center);
        cacheService.evictByPattern("donationCenters:*");
        auditPublisher.publish("CENTER_STATUS_UPDATED", id, "DonationCenter",
            Map.of("status", oldStatus != null ? oldStatus.name() : null),
            Map.of("status", status.name()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!centerRepository.existsById(id)) {
            throw new NotFoundException("Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        centerRepository.deleteById(id);
        cacheService.evictByPattern("donationCenters:*");
        cacheService.evictByPattern("slots:*");
        cacheService.evictByPattern("centerStaff:*");
        auditPublisher.publish("CENTER_DELETED", id, "DonationCenter", null, Map.of("deleted", true));
    }

    @Override
    @Transactional
    public Slot blockSlot(Long centerId, Long slotId, boolean isBlocked) {
        var center = centerRepository.findById(centerId).orElseThrow(() -> new NotFoundException(
                "Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name()));
        var slot = slotRepository.findById(slotId).orElseThrow(() -> new NotFoundException(
                "Slot not found: " + slotId, "SLOT_NOT_FOUND"));
        if (!slot.getCenterId().equals(centerId)) {
            throw new IllegalArgumentException("Slot does not belong to this center");
        }
        var wasBlocked = slot.isBlocked();
        slot.setBlocked(isBlocked);
        var saved = slotRepository.update(slot);
        cacheService.evictByPattern("slots:*");
        auditPublisher.publish("SLOT_BLOCKED", slotId, "DonationCenter",
            Map.of("blocked", wasBlocked),
            Map.of("blocked", isBlocked, "centerId", centerId));
        return saved;
    }

    @Override
    @Transactional
    public ClosureResult addClosure(Long centerId, ClosureCommand command) {
        var center = centerRepository.findById(centerId).orElseThrow(() -> new NotFoundException(
                "Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name()));
        var hours = center.getOperatingHours();
        List<OperatingHours.ClosureWindow> windows = hours.closedWindows();
        if (windows == null) windows = List.of();
        var newWindows = new java.util.ArrayList<>(windows);
        LocalTime start = command.startTime() != null ? LocalTime.parse(command.startTime(), DateTimeFormatter.ofPattern("HH:mm")) : null;
        LocalTime end = command.endTime() != null ? LocalTime.parse(command.endTime(), DateTimeFormatter.ofPattern("HH:mm")) : null;
        newWindows.add(new OperatingHours.ClosureWindow(command.date(), start, end, command.allDay(), command.reason()));
        var newHours = new OperatingHours(hours.monday(), hours.tuesday(), hours.wednesday(),
                hours.thursday(), hours.friday(), hours.saturday(), hours.sunday(), newWindows);
        center.setOperatingHours(newHours);
        centerRepository.save(center);
        cacheService.evictByPattern("donationCenters:*");
        cacheService.evictByPattern("slots:*");

        var startStr = command.startTime() != null ? command.startTime() : "00:00";
        var endStr = command.endTime() != null ? command.endTime() : "23:59";
        var overlapping = slotRepository.findOverlapping(centerId, command.date(), startStr, endStr);
        for (var slot : overlapping) {
            slot.setBlocked(true);
            slotRepository.update(slot);
        }
        auditPublisher.publish("CLOSURE_ADDED", centerId, "DonationCenter", null,
            Map.of("date", command.date().toString(), "reason", command.reason()));
        return new ClosureResult(overlapping.size(), command.date(), command.reason());
    }

    @Override
    @Transactional
    public CenterStaffProfile addStaff(Long centerId, Long userId) {
        if (!centerRepository.existsById(centerId)) {
            throw new NotFoundException("Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        if (centerRepository.existsStaffByCenterIdAndUserId(centerId, userId)) {
            throw new ConflictException("Staff already assigned to this center", "STAFF_ALREADY_ASSIGNED");
        }
        var staff = new CenterStaffProfile(userId, centerId);
        var saved = centerRepository.saveStaff(staff);
        cacheService.evictByPattern("centerStaff:*");
        auditPublisher.publish("STAFF_ADDED", saved.getId(), "DonationCenter", null,
            Map.of("centerId", centerId, "userId", userId));
        return saved;
    }

    @Override
    @Transactional
    public void removeStaff(Long centerId, Long userId) {
        var staff = centerRepository.findStaffByCenterIdAndUserId(centerId, userId)
                .orElseThrow(() -> new NotFoundException("Staff not found at this center", "STAFF_NOT_FOUND"));
        centerRepository.deleteStaff(staff);
        cacheService.evictByPattern("centerStaff:*");
        auditPublisher.publish("STAFF_REMOVED", null, "DonationCenter",
            Map.of("centerId", centerId, "userId", userId), null);
    }

    @Override
    @Transactional
    public DonationCenter approve(Long centerId, boolean approved, String reason) {
        var center = centerRepository.findById(centerId).orElseThrow(() -> new NotFoundException(
                "Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name()));
        var oldStatus = center.getStatus();
        center.setStatus(approved ? CenterStatus.ACTIVE : CenterStatus.CLOSED);
        var saved = centerRepository.save(center);
        cacheService.evictByPattern("donationCenters:*");
        auditPublisher.publish("CENTER_APPROVED", centerId, "DonationCenter",
            Map.of("status", oldStatus != null ? oldStatus.name() : null),
            Map.of("approved", approved, "reason", reason));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public DonationCenter getById(Long id) {
        var key = "donationCenters:" + id;
        var cached = cacheService.get(key, DonationCenter.class);
        if (cached.isPresent()) return cached.get();
        var result = centerRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public DonationCenter getById(Long id, boolean fetchJoins) {
        var key = "donationCenters:" + id + ":" + fetchJoins;
        var cached = cacheService.get(key, DonationCenter.class);
        if (cached.isPresent()) return cached.get();
        var result = centerRepository.findById(id, fetchJoins).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DonationCenter> getAll(SearchCriteria criteria) {
        return centerRepository.findAll(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DonationCenter> getPending(SearchCriteria criteria) {
        return centerRepository.findAllPending(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationCenter> getAllActive(Double lat, Double lng) {
        var centers = centerRepository.findAllByStatus(CenterStatus.ACTIVE);
        if (lat != null && lng != null) {
            return centers.stream().sorted(java.util.Comparator.comparingDouble(c -> haversineKm(lat, lng, c.getLatitude(), c.getLongitude()))).toList();
        }
        return centers;
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        if (lat2 == 0 && lng2 == 0) return Double.MAX_VALUE;
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Slot> getSlots(Long centerId, LocalDate date, String slotType, boolean fetchJoins) {
        var key = "slots:" + centerId + ":" + date + ":" + slotType + ":" + fetchJoins;
        var cached = cacheService.get(key, new TypeReference<List<Slot>>() {});
        if (cached.isPresent()) return cached.get();
        if (!centerRepository.existsById(centerId)) {
            throw new NotFoundException("Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        List<Slot> result;
        if (date != null) {
            if (fetchJoins) {
                result = slotRepository.findByCenterIdAndDateWithJoins(centerId, date);
            } else {
                result = slotRepository.findByCenterIdAndDate(centerId, date);
            }
        } else {
            var from = LocalDate.now();
            var to = from.plusWeeks(3);
            if (fetchJoins) {
                result = slotRepository.findByCenterIdAndDateRangeWithJoins(centerId, from, to);
            } else {
                result = slotRepository.findByCenterIdAndDateRange(centerId, from, to);
            }
        }
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CenterStaffProfile> getStaff(Long centerId) {
        var key = "centerStaff:" + centerId;
        var cached = cacheService.get(key, new TypeReference<List<CenterStaffProfile>>() {});
        if (cached.isPresent()) return cached.get();
        if (!centerRepository.existsById(centerId)) {
            throw new NotFoundException("Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        var result = centerRepository.findStaffByCenterId(centerId);
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CenterStaffProfile getStaffByUserId(Long userId) {
        return centerRepository.findStaffByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Staff profile not found for user: " + userId, "STAFF_NOT_FOUND"));
    }

    @Override
    @Transactional(readOnly = true)
    public CenterAdminProfile getAdminByUserId(Long userId) {
        return centerRepository.findAdminByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Admin profile not found for user: " + userId, "ADMIN_NOT_FOUND"));
    }

}
