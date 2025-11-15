package com.KimZo2.Back.exception.login;

public class DuplicateUserNickNameException extends RuntimeException {
    public DuplicateUserNickNameException(String message) {
        super(message);
    }
}
