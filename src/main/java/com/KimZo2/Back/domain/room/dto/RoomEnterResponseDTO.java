package com.KimZo2.Back.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomEnterResponseDTO {
    private UUID roomId;
    private String message;
    private String userId;
    private String nickName;
    private int avatar;
    private int count;
}
