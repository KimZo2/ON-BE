package com.KimZo2.Back.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisFunction {
    private final StringRedisTemplate redisTemplate;

    public boolean roomHasKey(String roomKey){
        boolean ok = redisTemplate.hasKey(roomKey);
        return Boolean.TRUE.equals(ok);
    }

    public boolean rommIsPrivate(String roomKey) {
        Map<Object, Object> roomHash = redisTemplate.opsForHash().entries(roomKey);
        return !"0".equals(String.valueOf(roomHash.getOrDefault("private", "0")));
    }
}
