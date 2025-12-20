package com.KimZo2.Back.domain.room.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RoomCreateDTO {
    private String name;
    private String creatorNickname;
    private int maxParticipants;
    private boolean isPrivate;
    private String password;
    private int roomTime;
    private int roomType;
}
