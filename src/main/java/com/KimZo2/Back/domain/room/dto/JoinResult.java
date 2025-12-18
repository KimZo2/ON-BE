package com.KimZo2.Back.domain.room.dto;

public record JoinResult(JoinStatus status, int count) {

    public enum JoinStatus {
        OK,
        ALREADY,
        FULL,
        CLOSED_OR_NOT_FOUND,
        ERROR
    }
}