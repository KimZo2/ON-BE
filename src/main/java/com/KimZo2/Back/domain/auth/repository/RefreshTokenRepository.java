package com.KimZo2.Back.domain.auth.repository;

public interface RefreshTokenRepository {
    void save(String memberId, String refreshToken, long expirationSeconds);
    String findByUserId(String memberId);
    void delete(String memberId);
}
