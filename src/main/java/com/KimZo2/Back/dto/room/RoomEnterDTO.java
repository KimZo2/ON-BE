package com.KimZo2.Back.dto.room;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class RoomEnterDTO {
    private UUID userId;
    private String password;
}
