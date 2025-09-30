package com.KimZo2.Back.exception.ws;

public class RoomNotFoundOrExpiredException extends RuntimeException {
    public RoomNotFoundOrExpiredException(String message) {
        super(message);
    }
}
