package com.KimZo2.Back.repository.redis;

import java.time.Duration;
import java.util.UUID;

public interface RoomStoreRepository {

    boolean lockRoomName(String roomName, String roomId, Duration roomTTL);

    void createRoomRuntime(UUID roomId, String roomName, boolean isPrivate, UUID creatorId, int max, int roomType, Duration ttl, long nowMs);

    void releaseNameLock(String roomName);

    void deleteRoomRuntimeIfPresent(UUID roomId);
}
