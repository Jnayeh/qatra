package com.zayenha.qatra.center.domain.port.out;

import com.zayenha.qatra.center.domain.model.DonationCenter;

import java.util.Optional;

public interface CenterRepositoryPort {
    DonationCenter save(DonationCenter center);
    boolean existsByName(String name);
    Optional<DonationCenter> findById(Long id);
}
