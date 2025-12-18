package com.KimZo2.Back.domain.roomparticipation.repository;

import com.KimZo2.Back.global.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomFunctionRepositoryImpl implements RoomFunctionRepository {
    private final StringRedisTemplate redisTemplate;

    @Override
    /** 방 존재 판단 */
    public boolean roomExists(UUID roomId) {
        Boolean b = redisTemplate.hasKey(KeyFactory.roomMeta(roomId));
        return Boolean.TRUE.equals(b);
    }

    @Override
    /** 방이 private인지  */
    public boolean roomIsPrivate(UUID roomId) {
        Object v = redisTemplate.opsForHash().get(KeyFactory.roomMeta(roomId), "visibility");
        // 기본값 "0" (public). null-safe 비교
        return "1".equals(String.valueOf(v));
    }

    @Override
    public Set<String> roomRecentHot(long from, long now) {
        String key = KeyFactory.roomHot();
        return redisTemplate.opsForZSet()
                .reverseRangeByScore(key, from, now);
    }

    @Override
    public Set<String> allRoomIds() {
        return redisTemplate.opsForSet().members(KeyFactory.roomActive());
    }
}
