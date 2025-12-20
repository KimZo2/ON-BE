package com.KimZo2.Back.domain.roomlogic.repository;

import com.KimZo2.Back.global.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PresenceRepositoryImpl implements PresenceRepository {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void updateSession(UUID roomId, UUID userId, String sessionId,
                              int presenceTtlSec, int userRoomTtlSec, long nowMs) {
        String presence = KeyFactory.presence(roomId,userId,sessionId);
        String seenKey     = KeyFactory.roomSeen(roomId);
        String userRoomKey = KeyFactory.userRoom(userId);
        String roomIdStr   = roomId.toString();

        redisTemplate.opsForValue().set(presence, "1", Duration.ofSeconds(presenceTtlSec));
        redisTemplate.opsForZSet().add(seenKey, userId.toString(), nowMs);
        redisTemplate.opsForValue().set(userRoomKey, roomIdStr, Duration.ofSeconds(userRoomTtlSec));
    }

    @Override
    public void deleteSession(UUID roomId, UUID userId, String sessionId) {
        redisTemplate.delete(KeyFactory.presence(roomId, userId, sessionId));
    }

    @Override
    public Set<String> findExpiredUserInRoom(String roomIdStr, int presenceTtlSec) {
        String seenKey = KeyFactory.roomSeen(UUID.fromString(roomIdStr));
        long expiredBefore = System.currentTimeMillis() - (presenceTtlSec * 1000L);
        Set<String> expiredUserIds = redisTemplate.opsForZSet().rangeByScore(seenKey, 0, expiredBefore);
        return expiredUserIds != null ? expiredUserIds : Set.of();
    }
}
