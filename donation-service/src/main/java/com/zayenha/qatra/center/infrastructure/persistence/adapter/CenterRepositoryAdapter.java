package com.zayenha.qatra.center.infrastructure.persistence.adapter;

import tools.jackson.databind.ObjectMapper;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.OperatingHours;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CenterRepositoryAdapter implements CenterRepositoryPort {

    private final ObjectMapper objectMapper;
    private final CenterJpaRepository jpaRepository;

    @Override
    public DonationCenter save(DonationCenter center) {
        var entity = toEntity(center);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public Optional<DonationCenter> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private CenterEntity toEntity(DonationCenter center) {
        var entity = new CenterEntity();
        entity.setId(center.getId());
        entity.setName(center.getName());
        entity.setAddress(center.getAddress());
        entity.setCity(center.getCity());
        entity.setCountry(center.getCountry());
        entity.setPostalCode(center.getPostalCode());
        entity.setPhone(center.getPhone());
        entity.setEmail(center.getEmail());
        entity.setLatitude(center.getLatitude());
        entity.setLongitude(center.getLongitude());
        entity.setFacilityType(center.getFacilityType());
        entity.setOperatingHours(center.getOperatingHours());
        entity.setStatus(center.getStatus());
        entity.setTotalCapacity(center.getTotalCapacity());
        entity.setMaxRegular(center.getMaxRegular());
        entity.setSlotPeriod(center.getSlotPeriod());
        entity.setCreatedAt(center.getCreatedAt());
        entity.setUpdatedAt(center.getUpdatedAt());
        return entity;
    }

    private DonationCenter toDomain(CenterEntity entity) {
        var center = new DonationCenter();
        center.setId(entity.getId());
        center.setName(entity.getName());
        center.setAddress(entity.getAddress());
        center.setCity(entity.getCity());
        center.setCountry(entity.getCountry());
        center.setPostalCode(entity.getPostalCode());
        center.setPhone(entity.getPhone());
        center.setEmail(entity.getEmail());
        center.setLatitude(entity.getLatitude());
        center.setLongitude(entity.getLongitude());
        center.setFacilityType(entity.getFacilityType());
        if (entity.getOperatingHours() != null) {
            try {
                center.setOperatingHours(
                    objectMapper.readValue(entity.getOperatingHours(), OperatingHours.class));
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize operating hours", e);
            }
        }
        center.setStatus(entity.getStatus());
        center.setTotalCapacity(entity.getTotalCapacity());
        center.setMaxRegular(entity.getMaxRegular());
        center.setSlotPeriod(entity.getSlotPeriod());
        center.setCreatedAt(entity.getCreatedAt());
        center.setUpdatedAt(entity.getUpdatedAt());
        return center;
    }
}
