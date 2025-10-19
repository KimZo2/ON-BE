package com.KimZo2.Back.security.jwt;

import com.KimZo2.Back.service.AuthService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
    private final Key key;
    private final long accessTokenExpTime;
    private final long refreshTokenExpTime;
    private final long clockSkewSeconds;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration_time}") long accessTokenExpTime,
            @Value("${jwt.clock-skew-seconds:60}") long clockSkewSeconds,
            @Value("${jwt.refresh_expiration_time}") long refreshTokenExpTime
    ){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpTime = accessTokenExpTime;
        this.refreshTokenExpTime = refreshTokenExpTime;
        this.clockSkewSeconds = clockSkewSeconds;
    }

    // JWT 생성 (액세스 토큰)
    public String createAccessToken(String userId, String nickname, String provider) {
        return createToken(userId, "access", Map.of(
                "nickname", nickname,
                "provider", provider
        ), accessTokenExpTime);
    }

    // JWT 생성 (리프레시 토큰)
    public String createRefreshToken(String userId) {
        return createToken(userId, "refresh", Map.of(), refreshTokenExpTime);
    }

    // JWT 생성 로직 통합
    private String createToken(String userId, String typ, Map<String, Object> claims, long expTime) {
        Instant now = Instant.now();
        claims.put("typ", typ);
        return Jwts.builder()
                .setSubject(userId)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expTime, ChronoUnit.SECONDS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(parseClaims(token).get("typ"));
        } catch (JwtException e) {
            log.warn("AccessToken 확인 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parseClaims(token).get("typ"));
        } catch (JwtException e) {
            log.warn("RefreshToken 확인 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        boolean valid = isAccessToken(token) && validate(token);
        if (!valid) log.warn("AccessToken 유효성 검증 실패: {}", token);
        return valid;
    }

    public boolean validateRefreshToken(String token) {
        boolean valid = isRefreshToken(token) && validate(token);
        if (!valid) log.warn("RefreshToken 유효성 검증 실패: {}", token);
        return valid;
    }

    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validate(String token) {
        try {
            parser().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("토큰 만료됨: {}", token);
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }


    public boolean isExpired(String token) {
        try {
            boolean expired = parseClaims(token).getExpiration().before(new Date());
            if (expired) log.info("토큰 만료 확인됨: {}", token);
            return expired;
        } catch (JwtException e) {
            log.warn("토큰 만료 체크 실패: {}", e.getMessage());
            return true;
        }
    }

    public String resolveBearer(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        return authorizationHeader.substring(7).trim();
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .build();
    }

}
