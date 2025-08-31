package com.KimZo2.Back.exception.room;

public class DuplicateRoomNameException extends RuntimeException {
    public DuplicateRoomNameException(String message) {
        super(message);
    }
}
