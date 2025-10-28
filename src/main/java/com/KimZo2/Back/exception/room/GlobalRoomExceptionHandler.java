package com.KimZo2.Back.exception.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalRoomExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRoomExceptionHandler.class);

    // 중복 이름 예외 처리
    @ExceptionHandler(DuplicateRoomNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateRoomName(DuplicateRoomNameException ex) {
        log.warn("Duplicate Room Name exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "DUPLICATE_ROOM_NAME",
                "message", ex.getMessage()
        ));
    }

    // 비밀번호 없는 Private Room 생성 시
    @ExceptionHandler(PasswordNotIncludeException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordNotInclude(PasswordNotIncludeException ex) {
        log.warn("Password Not Include exception for private room: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "PASSWORD_REQUIRED",
                "message", ex.getMessage()
        ));
    }

    // Redis 저장 실패 (RoomStoreFail)
    @ExceptionHandler(RoomStoreFailException.class)
    public ResponseEntity<Map<String, Object>> handleRoomStoreFail(RoomStoreFailException ex) {
        log.error("Room Store Fail exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "ROOM_STORE_FAIL",
                "message", ex.getMessage()
        ));
    }

    // 그 외 모든 Exception (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled general exception in room context: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", ex.getMessage()
        ));
    }
}
