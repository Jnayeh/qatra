package com.zayenha.qatra.center.infrastructure.persistence.adapter;

import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.shared.domain.SearchCriteria;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.shared.domain.PageResult;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CenterRepositoryAdapter implements CenterRepositoryPort {

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

    @Override
    public PageResult<DonationCenter> findAll(SearchCriteria criteria) {
        var spec = buildSpecification(criteria.search());
        var sort = buildSort(criteria.sortBy(), criteria.sortDirection());
        var pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        var page = jpaRepository.findAll(spec, pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    private Specification<CenterEntity> buildSpecification(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            var pattern = "%" + search.toLowerCase() + "%";
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            predicates.add(cb.like(cb.lower(root.get("address")), pattern));
            predicates.add(cb.like(cb.lower(root.get("city")), pattern));
            predicates.add(cb.like(cb.lower(root.get("country")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        var allowed = List.of("id", "name", "city", "country", "facilityType", "status", "createdAt", "updatedAt");
        var field = allowed.contains(sortBy) ? sortBy : "id";
        var dir = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field);
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
        center.setOperatingHours(entity.getOperatingHours());
        center.setStatus(entity.getStatus());
        center.setTotalCapacity(entity.getTotalCapacity());
        center.setMaxRegular(entity.getMaxRegular());
        center.setSlotPeriod(entity.getSlotPeriod());
        center.setCreatedAt(entity.getCreatedAt());
        center.setUpdatedAt(entity.getUpdatedAt());
        return center;
    }
}
