package com.KimZo2.Back.domain.roomparticipation.repository;

import java.util.Set;
import java.util.UUID;

public interface RoomFunctionRepository {

    boolean roomExists(UUID roomId);

    boolean roomIsPrivate(UUID roomId);

    Set<String> roomRecentHot(long from, long now);

    Set<String> allRoomIds();

    String getRoomName(UUID roomId);
}
