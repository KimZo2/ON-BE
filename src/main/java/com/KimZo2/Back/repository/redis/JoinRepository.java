package com.KimZo2.Back.repository.redis;

import com.KimZo2.Back.dto.room.JoinResult;

import java.util.UUID;

public interface JoinRepository {
    /** joinRoomLua: returns {code, count} */
    JoinResult join(UUID roomId, UUID userId, String nickname, String sessionId, int presenceTtlSec, int userRoomTtlSec, long nowMs);
}
