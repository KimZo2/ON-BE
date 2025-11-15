package com.KimZo2.Back.exception.chat;

public class InvalidRoomIdException extends RuntimeException {
    public InvalidRoomIdException(String message) {
        super(message);
    }
}