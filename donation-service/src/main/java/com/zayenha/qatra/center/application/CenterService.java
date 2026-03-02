package com.zayenha.qatra.center.application;

import com.zayenha.qatra.center.domain.exception.CenterErrorCode;
import com.zayenha.qatra.center.domain.model.*;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases;
import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.domain.service.CenterDomainValidator;
import com.zayenha.qatra.center.infrastructure.persistence.adapter.SlotRepositoryAdapter;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CenterService implements CenterCommandUseCases, CenterQueryUseCases {

    private final CenterRepositoryPort centerRepository;
    private final SlotRepositoryAdapter slotRepositoryAdapter;

    private CenterDomainValidator validator() {
        return new CenterDomainValidator(centerRepository);
    }

    @Override
    @Transactional
    public DonationCenter create(CreateCenterCommand command) {
        validator().validateCreate(command.name());
        var center = new DonationCenter(
            command.name(), command.address(), command.city(),
            command.country(), command.postalCode(), command.phone(),
            command.email(), command.latitude(), command.longitude(),
            command.facilityType(), command.operatingHours(),
            command.totalCapacity(), command.maxRegular(), command.slotPeriod()
        );
        return centerRepository.save(center);
    }

    @Override
    @Transactional
    public DonationCenter update(Long id, UpdateCenterCommand command) {
        validator().validateUpdate(id, command.name());
        var center = centerRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
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
        return centerRepository.save(center);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, CenterStatus status) {
        var center = centerRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
        center.setStatus(status);
        centerRepository.save(center);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!centerRepository.existsById(id)) {
            throw new NotFoundException("Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        centerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Slot blockSlot(Long centerId, Long slotId, boolean isBlocked) {
        var center = centerRepository.findById(centerId).orElseThrow(() -> new NotFoundException(
                "Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name()));
        var slot = slotRepositoryAdapter.findById(slotId).orElseThrow(() -> new NotFoundException(
                "Slot not found: " + slotId, "SLOT_NOT_FOUND"));
        if (!slot.getCenterId().equals(centerId)) {
            throw new IllegalArgumentException("Slot does not belong to this center");
        }
        slot.setBlocked(isBlocked);
        return slotRepositoryAdapter.update(slot);
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

        var startStr = command.startTime() != null ? command.startTime() : "00:00";
        var endStr = command.endTime() != null ? command.endTime() : "23:59";
        var overlapping = slotRepositoryAdapter.findOverlapping(centerId, command.date(), startStr, endStr);
        for (var slot : overlapping) {
            slot.setBlocked(true);
            slotRepositoryAdapter.update(slot);
        }
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
        return centerRepository.saveStaff(staff);
    }

    @Override
    @Transactional
    public void removeStaff(Long centerId, Long userId) {
        var staff = centerRepository.findStaffByCenterIdAndUserId(centerId, userId)
                .orElseThrow(() -> new NotFoundException("Staff not found at this center", "STAFF_NOT_FOUND"));
        centerRepository.deleteStaff(staff);
    }

    @Override
    @Transactional
    public DonationCenter approve(Long centerId, boolean approved, String reason) {
        var center = centerRepository.findById(centerId).orElseThrow(() -> new NotFoundException(
                "Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name()));
        center.setStatus(approved ? CenterStatus.ACTIVE : CenterStatus.CLOSED);
        return centerRepository.save(center);
    }

    @Override
    @Transactional(readOnly = true)
    public DonationCenter getById(Long id) {
        return centerRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public DonationCenter getById(Long id, boolean fetchJoins) {
        return centerRepository.findById(id, fetchJoins).orElseThrow(() -> new NotFoundException(
                "Center not found: " + id, CenterErrorCode.CENTER_NOT_FOUND.name()));
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
    public List<Slot> getSlots(Long centerId, LocalDate date, String slotType, boolean fetchJoins) {
        if (!centerRepository.existsById(centerId)) {
            throw new NotFoundException("Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        if (date != null) {
            if (fetchJoins) {
                return slotRepositoryAdapter.findByCenterIdAndDateWithJoins(centerId, date);
            }
            return slotRepositoryAdapter.findByCenterIdAndDate(centerId, date);
        }
        var from = LocalDate.now();
        var to = from.plusWeeks(3);
        if (fetchJoins) {
            return slotRepositoryAdapter.findByCenterIdAndDateRangeWithJoins(centerId, from, to);
        }
        return slotRepositoryAdapter.findByCenterIdAndDateRange(centerId, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CenterStaffProfile> getStaff(Long centerId) {
        if (!centerRepository.existsById(centerId)) {
            throw new NotFoundException("Center not found: " + centerId, CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        return centerRepository.findStaffByCenterId(centerId);
    }
}
