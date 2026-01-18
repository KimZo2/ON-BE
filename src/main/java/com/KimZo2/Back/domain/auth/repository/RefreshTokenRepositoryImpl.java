package com.KimZo2.Back.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    @Qualifier("redisTemplateForDb1")
    private final RedisTemplate<String, Object> redisTemplateForDb1;
    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    @Override
    public void save(String memberId, String refreshToken, long expirationSeconds) {
        ValueOperations<String, Object> ops = redisTemplateForDb1.opsForValue();
        ops.set(REFRESH_TOKEN_PREFIX + memberId, refreshToken, expirationSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String findByUserId(String memberId) {
        ValueOperations<String, Object> ops = redisTemplateForDb1.opsForValue();
        Object value = ops.get(REFRESH_TOKEN_PREFIX + memberId);
        return value != null ? value.toString() : null;
    }

    @Override
    public void delete(String memberId) {
        redisTemplateForDb1.delete(REFRESH_TOKEN_PREFIX + memberId);
    }
}