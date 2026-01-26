package com.zayenha.qatra.user.infrastructure.web.exception;

import com.zayenha.qatra.shared.web.ApiResponse;
import com.zayenha.qatra.user.domain.exception.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyExists(AlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({CannotDeleteActiveUserException.class})
    public ResponseEntity<ApiResponse<Void>> handleDomainRuleViolation(RulesViolationException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
