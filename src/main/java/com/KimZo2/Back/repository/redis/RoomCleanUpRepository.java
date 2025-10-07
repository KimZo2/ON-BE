package com.KimZo2.Back.repository.redis;

import java.util.UUID;

public interface RoomCleanUpRepository {

    Long cleanupExpiredUser(UUID roomId, String userId);
}
