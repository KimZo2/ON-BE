package com.KimZo2.Back.domain.room.dto;

public record RoomListItemResponse(
        String roomId,
        String roomName,
        int roomCurrentPersonCnt,
        int roomMaximumPersonCnt,
        int roomBackgroundImg
) {}
