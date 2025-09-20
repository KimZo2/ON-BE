package com.KimZo2.Back.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RoomPosResponseDTO {
    private UUID userId;
    private double x;
    private double y;
    private long seq;
    private String direction;
    private boolean isMoving;
}
