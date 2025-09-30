package com.KimZo2.Back.exception.room;

public class PasswordNotIncludeException extends RuntimeException {
    public PasswordNotIncludeException(String message) {
        super(message);
    }
}
