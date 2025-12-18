package com.KimZo2.Back.domain.roomlogic.repository;

import java.util.Set;
import java.util.UUID;

public interface PresenceRepository {
    public void updateSession(UUID roomId, UUID userId, String sessionId,
                              int presenceTtlSec, int userRoomTtlSec, long nowMs);

    void deleteSession(UUID roomId, UUID userId, String sessionId);

    Set<String> findExpiredUserInRoom(String roomIdStr, int presenceTtlSec);
}

