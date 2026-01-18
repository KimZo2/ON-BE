package com.KimZo2.Back.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RoomPosResponseDTO {
    private UUID userId;
    private String nickname;
    private double x;
    private double y;
    private long seq;
    private String direction;
    private int avatar;
    private boolean isMoving;
}
