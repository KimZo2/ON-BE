package com.KimZo2.Back.exception.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalUserExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalUserExceptionHandler.class);

    @ExceptionHandler(DuplicateUserNicknameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateUserNicknameException(DuplicateUserNicknameException ex) {
        log.warn("Duplicate User Nickname exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "DUPLICATE_USER_NICKNAME",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User Not Found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "USER_NOT_FOUND",
                "message", ex.getMessage()
        ));
    }
}
