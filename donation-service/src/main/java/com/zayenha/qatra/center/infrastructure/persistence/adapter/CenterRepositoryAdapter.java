package com.zayenha.qatra.center.infrastructure.persistence.adapter;

import com.zayenha.qatra.center.domain.model.CenterAdminProfile;
import com.zayenha.qatra.center.domain.model.CenterStaffProfile;
import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.DonationCenter;
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
    private final CenterMapper mapper;

    @Override
    public DonationCenter save(DonationCenter center) {
        var entity = mapper.toEntity(center);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
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
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<DonationCenter> findById(Long id, boolean fetchJoins) {
        if (fetchJoins) {
            return jpaRepository.findWithSlotsById(id).map(mapper::toDomain);
        }
        return jpaRepository.findById(id).map(mapper::toDomain);
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
            page.getContent().stream().map(mapper::toDomain).toList(),
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
            page.getContent().stream().map(mapper::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public CenterStaffProfile saveStaff(CenterStaffProfile staff) {
        var entity = mapper.toStaffEntity(staff);
        var saved = staffJpaRepository.save(entity);
        return mapper.toStaffDomain(saved);
    }

    @Override
    public List<CenterStaffProfile> findStaffByCenterId(Long centerId) {
        return staffJpaRepository.findByCenter_Id(centerId).stream().map(mapper::toStaffDomain).toList();
    }

    @Override
    public Optional<CenterStaffProfile> findStaffByCenterIdAndUserId(Long centerId, Long userId) {
        return staffJpaRepository.findByCenter_IdAndUser_Id(centerId, userId).map(mapper::toStaffDomain);
    }

    @Override
    public boolean existsStaffByCenterIdAndUserId(Long centerId, Long userId) {
        return staffJpaRepository.existsByCenter_IdAndUser_Id(centerId, userId);
    }

    @Override
    public void deleteStaff(CenterStaffProfile staff) {
        staffJpaRepository.deleteByCenter_IdAndUser_Id(staff.getCenterId(), staff.getUserId());
    }

    @Override
    public CenterAdminProfile saveAdmin(CenterAdminProfile admin) {
        var entity = mapper.toAdminEntity(admin);
        var saved = adminJpaRepository.save(entity);
        return mapper.toAdminDomain(saved);
    }

    @Override
    public Optional<CenterAdminProfile> findAdminByUserId(Long userId) {
        return adminJpaRepository.findByUser_Id(userId).map(mapper::toAdminDomain);
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
}
