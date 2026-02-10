package com.zayenha.qatra.center.domain.port.out;

import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.shared.domain.SearchCriteria;

import java.util.Optional;

public interface CenterRepositoryPort {
    DonationCenter save(DonationCenter center);
    boolean existsByName(String name);
    boolean existsById(Long id);
    boolean otherCenterHasName(Long id, String name);
    Optional<DonationCenter> findById(Long id);
    PageResult<DonationCenter> findAll(SearchCriteria criteria);
    void deleteById(Long id);
}
