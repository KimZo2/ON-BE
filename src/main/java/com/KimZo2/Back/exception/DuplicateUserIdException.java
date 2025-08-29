package com.KimZo2.Back.exception;

public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(String message) {
        super(message);
    }
}

/**
*코드에서 예외 발생
 * 1.
 * if (userIdExists) {
 *     throw new DuplicateUserIdException("이미 존재하는 아이디입니다.");
 * }
 * 2.
 * Spring 이 GlobalExceptionHandler 에게 전달
 *
 * 3.
 * Handler 가 ResponseEntity 로 바꿔서 클라이언트에게 전달
 * → HTTP 400 Bad Request + 메시지
* */