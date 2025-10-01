package com.KimZo2.Back.exception.user;

public class DuplicateUserNicknameException extends RuntimeException {
    public DuplicateUserNicknameException(String message) {
        super(message);
    }
}
