package com.zayenha.qatra.center.domain.service;

import com.zayenha.qatra.center.domain.exception.CenterErrorCode;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.shared.exception.ConflictException;
import com.zayenha.qatra.shared.exception.NotFoundException;

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

    public void validateUpdate(Long id, String name) {
        if (centerRepository.otherCenterHasName(id, name)) {
            throw new ConflictException("Other center already named: " + name,
                    CenterErrorCode.CENTER_NAME_ALREADY_EXISTS.name());
        }
    }
}
