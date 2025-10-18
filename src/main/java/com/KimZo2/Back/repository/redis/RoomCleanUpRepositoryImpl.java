package com.KimZo2.Back.repository.redis;

// RoomCleanupRepositoryImpl.java

import com.KimZo2.Back.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoomCleanUpRepositoryImpl implements RoomCleanUpRepository {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> cleanupUserLua;

    @Override
    public Long cleanupExpiredUser(UUID roomId, String userId) {
        // Lua 스크립트에 전달할 KEYS 리스트
        List<String> keys = List.of(
                KeyFactory.roomMembers(roomId),         // KEYS[1]
                KeyFactory.roomPos(roomId),             // KEYS[2]
                KeyFactory.roomMeta(roomId),            // KEYS[3]
                KeyFactory.roomSeen(roomId),            // KEYS[4]
                KeyFactory.userRoom(UUID.fromString(userId)), // KEYS[5]
                KeyFactory.roomNicknames(roomId)        // KEYS[6]
        );

        // 스크립트를 실행하고 결과를 바로 반환
        return redisTemplate.execute(cleanupUserLua, keys, userId);
    }
}