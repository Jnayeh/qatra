package com.zayenha.qatra.user.domain.exception;

public enum UserErrorCode {
    USER_NOT_FOUND,
    EMAIL_ALREADY_EXISTS,
    PHONE_ALREADY_EXISTS,
    INVALID_ROLE_ASSIGNMENT,
    CANNOT_DELETE_ACTIVE_USER,
    USER_CONFLICT;
}
