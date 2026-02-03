package com.zayenha.qatra.center.domain.service;

import com.zayenha.qatra.center.domain.exception.CenterErrorCode;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.shared.exception.ConflictException;

public class CenterDomainValidator {

    private final CenterRepositoryPort centerRepository;

    public CenterDomainValidator(CenterRepositoryPort centerRepository) {
        this.centerRepository = centerRepository;
    }

    public void validateCreate(String name) {
        if (centerRepository.existsByName(name)) {
            throw new ConflictException("Center name already exists: " + name,
                    CenterErrorCode.CENTER_NAME_ALREADY_EXISTS.name());
        }
    }
}
