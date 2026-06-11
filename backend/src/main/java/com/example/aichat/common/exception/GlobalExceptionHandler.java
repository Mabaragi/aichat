package com.example.aichat.common.exception;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception) {
        return response(status(exception.getCode()), exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return response(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_DEBATE_RULE, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException exception) {
        return response(
                HttpStatus.CONFLICT,
                ErrorCode.EMAIL_ALREADY_EXISTS,
                "Email is already registered"
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            ObjectOptimisticLockingFailureException exception) {
        return response(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_TOKEN,
                "Token is invalid or expired"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return response(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_DEBATE_RULE,
                exception.getMessage()
        );
    }

    private static ResponseEntity<ErrorResponse> response(
            HttpStatus status, ErrorCode code, String message) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(code, message, LocalDateTime.now())
        );
    }

    private static HttpStatus status(ErrorCode code) {
        return switch (code) {
            case EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_CREDENTIALS, INVALID_TOKEN, REFRESH_TOKEN_REUSED, UNAUTHORIZED ->
                    HttpStatus.UNAUTHORIZED;
            case USER_NOT_FOUND, CATEGORY_NOT_FOUND, CHARACTER_NOT_FOUND, DEBATE_SESSION_NOT_FOUND,
                 DEBATE_PARTICIPANT_NOT_FOUND, SHARED_CONTENT_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;
            case INVALID_SESSION_STATE -> HttpStatus.CONFLICT;
            case TURN_GENERATION_FAILED -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
