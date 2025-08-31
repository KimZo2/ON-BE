package com.KimZo2.Back.exception.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateUserIdException.class)
    public ResponseEntity<?> handleDuplicateUserId(DuplicateUserIdException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // ResponseCode 409
                .body(e.getMessage());
    }

    @ExceptionHandler(DuplicateUserNickNameException.class)
    public ResponseEntity<?> handleDuplicateNickName(DuplicateUserIdException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // ResponseCode 409
                .body(e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<?> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(AdditionalSignupRequiredException.class)
    public ResponseEntity<?> handleAdditionalSignupRequired(AdditionalSignupRequiredException e) {
        log.warn("🚨 추가 회원가입 예외 발생: provider={}, id={}", e.getProvider(), e.getProviderId());

        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED) // ResponseCode 428
                .body(Map.of(
                        "provider", e.getProvider(),
                        "providerId", e.getProviderId()
                ));
    }


    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleDuplicateKeyException(DuplicateKeyException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(Map.of("error", "이미 존재하는 닉네임입니다."));
    }
}
