package com.KimZo2.Back.dto.room;

public record RoomListItemResponse(
        String roomId,
        String roomName,
        int roomCurrentPersonCnt,
        int roomMaximumPersonCnt,
        int roomBackgroundImg
) {}
