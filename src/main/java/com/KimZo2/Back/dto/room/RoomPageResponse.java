package com.KimZo2.Back.dto.room;

import java.util.List;

public record RoomPageResponse(
        int page,
        int size,
        int totalElement,
        boolean hasNext,
        List<RoomListItemResponse> rooms
) {}
