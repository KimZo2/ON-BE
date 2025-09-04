package com.KimZo2.Back.exception.ws;

public class RoomAccessDeniedException extends RuntimeException {
  public RoomAccessDeniedException(String message) {
    super(message);
  }
}
