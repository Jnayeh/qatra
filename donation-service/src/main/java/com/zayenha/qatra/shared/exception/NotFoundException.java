package com.zayenha.qatra.shared.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String entityType, Object id) {
        super(entityType + " not found: " + id);
    }
}
