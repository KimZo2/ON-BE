package com.KimZo2.Back.global.jwt;


import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io. jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    private Key accessKey;
    private Key refreshKey;

    @PostConstruct
    public void init() throws Exception {
        if (jwtProperties.getSecret() == null) {
            throw new IllegalStateException("JWT secret cannot be null");
        }
        if (jwtProperties.getRefreshSecret() == null) { // Refresh Secret null check
            throw new IllegalStateException("JWT refresh secret cannot be null");
        }
        // [임시 디버깅 로그] 설정된 토큰 유효기간 값을 확인합니다.
        log.info("Loaded Access Token Validity (seconds): {}", jwtProperties.getAccessTokenValidityInSeconds());
        log.info("Loaded Refresh Token Validity (seconds): {}", jwtProperties.getRefreshTokenValidityInSeconds());

        // Access Token Key
        byte[] accessKeyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);

        // Refresh Token Key
        byte[] refreshKeyBytes = Decoders.BASE64.decode(jwtProperties.getRefreshSecret());
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
    }

    // JWT 생성 (액세스 토큰)
    public String createAccessToken(String userId, String nickname, String provider) {
        long now = System.currentTimeMillis();
        long validity = jwtProperties.getAccessTokenValidityInSeconds() * 1000L;
        Date expiration = new Date(now + validity);

        log.info("Creating Access Token. Now: {}, Validity: {}ms, Expiration: {}", new Date(now), validity, expiration);

        return createToken(userId, "access", Map.of(
                "nickname", nickname,
                "provider", provider
        ), expiration);
    }

    // JWT 생성 (리프레시 토큰)
    public String createRefreshToken(String userId) {
        long now = System.currentTimeMillis();
        long validity = jwtProperties.getRefreshTokenValidityInSeconds() * 1000L;
        Date expiration = new Date(now + validity);

        return createToken(userId, "refresh", Map.of(), expiration);
    }

    // JWT 생성 로직 통합
    private String createToken(String userId, String typ, Map<String, Object> claims, Date expiration) {
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put("typ", typ);
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId)
                .addClaims(tokenClaims)
                .setIssuedAt(Date.from(now))
                .setExpiration(expiration)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // accessToken 인증정보 추출
    public Authentication getAuthenticationFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Null-safe 하게 권한 정보 추출
        String authoritiesClaim = claims.get("authorities", String.class);
        Collection<? extends GrantedAuthority> authorities;

        if (authoritiesClaim == null || authoritiesClaim.trim().isEmpty()) {
            authorities = Collections.emptyList(); // 권한이 없는 경우
        } else {
            authorities = Arrays.stream(authoritiesClaim.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // Spring Security의 User 객체 사용 (UserDetails 구현체)
        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 유효성 검사 (용도별 분리)
    public boolean validateAccessToken(String token) {
        return validate(token, accessKey);
    }

    public boolean validateRefreshToken(String token) { return validate(token, refreshKey); }


    public boolean validate(String token, Key key) {
        if (token == null || token.trim().isEmpty()) {

        }
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException | SecurityException e) {
            log.error("JWT 서명 검증 실패", e);
            throw new CustomException(ErrorCode.INVALID_TOKEN_SIGNATURE);
        } catch (MalformedJwtException e) {
            log.error("JWT 형식 오류", e);
            throw new CustomException(ErrorCode.MALFORMED_TOKEN_ERROR);
        } catch (ExpiredJwtException e) {
            log.error("JWT 만료", e);
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰 형식", e);
            throw new CustomException(ErrorCode.UNSUPPORTED_TOKEN_ERROR);
        } catch (IllegalArgumentException e) {
            log.error("JWT 파싱 실패 - 비어있는 토큰 등", e);
            throw new CustomException(ErrorCode.TOKEN_PARSING_FAILED);
        }
    }

    //  토큰에서 userId(subject) 추출 (만료된 토큰에서도 추출 가능하도록 수정)
    public String extractUserId(String token, boolean isRefresh) {
        Key key = isRefresh ? refreshKey : accessKey;
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 subject(userId)는 반환
            return e.getClaims().getSubject();
        }
    }

    // 토큰에서 만료 시간 추출 (만료된 토큰에서도 추출 가능하도록 수정)
    public Date extractExpiration(String token, boolean isRefresh) {
        Key key = isRefresh ? refreshKey : accessKey;
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 만료 시간은 반환
            return e.getClaims().getExpiration();
        }
    }

}
