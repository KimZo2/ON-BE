package com.KimZo2.Back.domain.roomparticipation.repository;

import java.util.UUID;

public interface RoomCleanUpRepository {
    Long cleanupExpiredUser(UUID roomId, String userId);
    void deleteAllRoomData(UUID roomId);
}
