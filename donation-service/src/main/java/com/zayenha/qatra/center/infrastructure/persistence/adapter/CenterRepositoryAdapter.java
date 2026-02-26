package com.zayenha.qatra.center.infrastructure.persistence.adapter;

import com.zayenha.qatra.center.domain.model.CenterAdminProfile;
import com.zayenha.qatra.center.domain.model.CenterStaffProfile;
import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.OperatingHours;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterAdminProfileEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterStaffProfileEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterAdminJpaRepository;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterStaffJpaRepository;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
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
    private final CenterStaffJpaRepository staffJpaRepository;
    private final CenterAdminJpaRepository adminJpaRepository;

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
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean otherCenterHasName(Long id, String name) {
        return jpaRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public Optional<DonationCenter> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<DonationCenter> findById(Long id, boolean fetchJoins) {
        if (fetchJoins) {
            return jpaRepository.findWithSlotsById(id).map(this::toDomain);
        }
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public PageResult<DonationCenter> findAll(SearchCriteria criteria) {
        var spec = buildSpecification(criteria.search());
        var sort = buildSort(criteria.sortBy(), criteria.sortDirection());
        var pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        var page = jpaRepository.findAll(spec, pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public PageResult<DonationCenter> findAllPending(SearchCriteria criteria) {
        var sort = buildSort(criteria.sortBy(), criteria.sortDirection());
        var pageable = PageRequest.of(criteria.page(), criteria.size(), sort);
        var page = jpaRepository.findByStatus(CenterStatus.PENDING_APPROVAL, pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public CenterStaffProfile saveStaff(CenterStaffProfile staff) {
        var entity = toStaffEntity(staff);
        var saved = staffJpaRepository.save(entity);
        return toStaffDomain(saved);
    }

    @Override
    public List<CenterStaffProfile> findStaffByCenterId(Long centerId) {
        return staffJpaRepository.findByCenterId(centerId).stream().map(this::toStaffDomain).toList();
    }

    @Override
    public Optional<CenterStaffProfile> findStaffByCenterIdAndUserId(Long centerId, Long userId) {
        return staffJpaRepository.findByCenterIdAndUserId(centerId, userId).map(this::toStaffDomain);
    }

    @Override
    public boolean existsStaffByCenterIdAndUserId(Long centerId, Long userId) {
        return staffJpaRepository.existsByCenterIdAndUserId(centerId, userId);
    }

    @Override
    public void deleteStaff(CenterStaffProfile staff) {
        staffJpaRepository.deleteByCenterIdAndUserId(staff.getCenterId(), staff.getUserId());
    }

    @Override
    public CenterAdminProfile saveAdmin(CenterAdminProfile admin) {
        var entity = toAdminEntity(admin);
        var saved = adminJpaRepository.save(entity);
        return toAdminDomain(saved);
    }

    @Override
    public Optional<CenterAdminProfile> findAdminByUserId(Long userId) {
        return adminJpaRepository.findByUserId(userId).map(this::toAdminDomain);
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

    private CenterStaffProfileEntity toStaffEntity(CenterStaffProfile staff) {
        var entity = new CenterStaffProfileEntity();
        entity.setId(staff.getId());
        entity.setUserId(staff.getUserId());
        entity.setCenter(jpaRepository.getReferenceById(staff.getCenterId()));
        entity.setVerified(staff.isVerified());
        return entity;
    }

    private CenterStaffProfile toStaffDomain(CenterStaffProfileEntity entity) {
        var staff = new CenterStaffProfile();
        staff.setId(entity.getId());
        staff.setUserId(entity.getUserId());
        staff.setCenterId(entity.getCenter().getId());
        staff.setVerified(entity.isVerified());
        staff.setCreatedAt(entity.getCreatedAt());
        return staff;
    }

    private CenterAdminProfileEntity toAdminEntity(CenterAdminProfile admin) {
        var entity = new CenterAdminProfileEntity();
        entity.setId(admin.getId());
        entity.setUserId(admin.getUserId());
        entity.setCenter(jpaRepository.getReferenceById(admin.getCenterId()));
        return entity;
    }

    private CenterAdminProfile toAdminDomain(CenterAdminProfileEntity entity) {
        var admin = new CenterAdminProfile();
        admin.setId(entity.getId());
        admin.setUserId(entity.getUserId());
        admin.setCenterId(entity.getCenter().getId());
        admin.setCreatedAt(entity.getCreatedAt());
        return admin;
    }
}
