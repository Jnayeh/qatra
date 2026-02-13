package com.zayenha.qatra.center.application;

import com.zayenha.qatra.center.domain.exception.CenterErrorCode;
import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.shared.domain.SearchCriteria;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases.CreateCenterCommand;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases.UpdateCenterCommand;
import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.domain.service.CenterDomainValidator;
import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CenterService implements CenterCommandUseCases, CenterQueryUseCases {

    private final CenterRepositoryPort centerRepository;

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

        var center = centerRepository.findById(id).orElseThrow(()->new NotFoundException(
                "Center not found: " + id,
                CenterErrorCode.CENTER_NOT_FOUND.name()));
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
        var center = centerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Center not found: " + id,
                        CenterErrorCode.CENTER_NOT_FOUND.name()));
        center.setStatus(status);
        centerRepository.save(center);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!centerRepository.existsById(id)) {
            throw new NotFoundException("Center not found: " + id,
                    CenterErrorCode.CENTER_NOT_FOUND.name());
        }
        centerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DonationCenter getById(Long id) {
        return centerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Center not found: " + id,
                        CenterErrorCode.CENTER_NOT_FOUND.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DonationCenter> getAll(SearchCriteria criteria) {
        return centerRepository.findAll(criteria);
    }
}
