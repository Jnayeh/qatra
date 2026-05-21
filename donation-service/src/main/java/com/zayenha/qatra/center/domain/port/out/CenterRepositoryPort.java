package com.zayenha.qatra.center.domain.port.out;

import com.zayenha.qatra.center.domain.model.*;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;

import java.util.List;
import java.util.Optional;

public interface CenterRepositoryPort {
    DonationCenter save(DonationCenter center);
    boolean existsByName(String name);
    boolean existsById(Long id);
    boolean otherCenterHasName(Long id, String name);
    Optional<DonationCenter> findById(Long id);
    Optional<DonationCenter> findById(Long id, boolean fetchJoins);
    PageResult<DonationCenter> findAll(SearchCriteria criteria);
    PageResult<DonationCenter> findAllPending(SearchCriteria criteria);
    List<DonationCenter> findAllByStatus(CenterStatus status);
    void deleteById(Long id);

    CenterStaffProfile saveStaff(CenterStaffProfile staff);
    List<CenterStaffProfile> findStaffByCenterId(Long centerId);
    Optional<CenterStaffProfile> findStaffByCenterIdAndUserId(Long centerId, Long userId);
    boolean existsStaffByCenterIdAndUserId(Long centerId, Long userId);
    void deleteStaff(CenterStaffProfile staff);

    CenterAdminProfile saveAdmin(CenterAdminProfile admin);
    Optional<CenterAdminProfile> findAdminByUserId(Long userId);
}
