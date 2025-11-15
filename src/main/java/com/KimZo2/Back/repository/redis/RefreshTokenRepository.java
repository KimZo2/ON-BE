package com.KimZo2.Back.repository.redis;

public interface RefreshTokenRepository {
    void save(String userId, String refreshToken, long expirationSeconds);
    String findByUserId(String userId);
    void delete(String userId);
}
