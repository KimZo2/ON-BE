package com.KimZo2.Back.dto.room;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class RoomEnterResponseDTO {
    private UUID roomId;
    private String message;
    private int count;
}
