package com.KimZo2.Back.repository.redis;

import com.KimZo2.Back.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
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
}
