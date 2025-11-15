package com.KimZo2.Back.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

public class GlobalUserExceptionHandler {
    @ExceptionHandler(DuplicateUserNicknameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateUserNicknameException(DuplicateUserNicknameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "DUPLICATE_ROOM_NAME",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "DUPLICATE_USER_NICKNAME",
                "message", ex.getMessage()
        ));
    }
}
