package com.KimZo2.Back.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        // 부모 생성자(RuntimeException)에 에러 메시지를 전달합니다.
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
