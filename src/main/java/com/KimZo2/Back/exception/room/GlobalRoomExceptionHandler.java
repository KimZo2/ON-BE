package com.KimZo2.Back.exception.room;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalRoomExceptionHandler {

    // 중복 이름 예외 처리
    @ExceptionHandler(DuplicateRoomNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateRoomName(DuplicateRoomNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "DUPLICATE_ROOM_NAME",
                "message", ex.getMessage()
        ));
    }

    // 비밀번호 없는 Private Room 생성 시
    @ExceptionHandler(PasswordNotIncludeException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordNotInclude(PasswordNotIncludeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "PASSWORD_REQUIRED",
                "message", ex.getMessage()
        ));
    }

    // Redis 저장 실패 (RoomStoreFail)
    @ExceptionHandler(RoomStoreFailException.class)
    public ResponseEntity<Map<String, Object>> handleRoomStoreFail(RoomStoreFailException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "ROOM_STORE_FAIL",
                "message", ex.getMessage()
        ));
    }

    // 그 외 모든 Exception (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", ex.getMessage()
        ));
    }
}
