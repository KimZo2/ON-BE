package com.KimZo2.Back.domain.roomparticipation.repository;

import com.KimZo2.Back.domain.room.dto.JoinResult;

import java.util.UUID;

public interface JoinRepository {
    /** joinRoomLua: returns {code, count} */
    JoinResult join(UUID roomId, UUID userId, String nickname, String sessionId, int presenceTtlSec, int userRoomTtlSec, long nowMs);
}
