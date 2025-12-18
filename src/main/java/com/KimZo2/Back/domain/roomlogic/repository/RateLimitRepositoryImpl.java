package com.KimZo2.Back.domain.roomlogic.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
class RateLimitRepositoryImpl implements RateLimitRepository {
    private final StringRedisTemplate redis;

    @Override
    public long incrWithWindow(String key, int windowSec) {
        Long c = redis.opsForValue().increment(key);
        if (c != null && c == 1L) {
            redis.expire(key, Duration.ofSeconds(windowSec));
        }
        return c == null ? 0L : c;
    }
}