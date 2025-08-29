package com.KimZo2.Back.dto.room;

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
