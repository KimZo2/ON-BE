package com.KimZo2.Back.service;

import com.KimZo2.Back.dto.auth.*;
import com.KimZo2.Back.dto.member.LoginResponseDTO;
import com.KimZo2.Back.model.User;
import com.KimZo2.Back.exception.login.AdditionalSignupRequiredException;
import com.KimZo2.Back.repository.UserRepository;
import com.KimZo2.Back.repository.redis.RefreshTokenRepository;
import com.KimZo2.Back.security.jwt.JwtUtil;
import com.KimZo2.Back.util.GitHubUtil;
import com.KimZo2.Back.util.GoogleUtil;
import com.KimZo2.Back.util.KakaoUtil;
import com.KimZo2.Back.util.NaverUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Value("${jwt.expiration_time}")
    private long tokenExpireTime;
    @Value("${jwt.refresh_expiration_time}")
    private long refreshTokenExpTime;

    private final KakaoUtil kakaoUtil;
    private final NaverUtil naverUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GitHubUtil gitHubUtil;
    private final GoogleUtil googleUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponseDTO oAuthLoginWithKakao(String accessCode, HttpServletResponse response) {
        log.info("AuthService - 카카오 인증 실행");

        // access_token 요청
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);

        // 사용자 정보 요청
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        // provider && providerId
        String provider = "kakao";
        String providerId = String.valueOf(kakaoProfile.getId());

        return handleSocialLogin(provider, providerId, response);
    }

    public LoginResponseDTO oAuthLoginWithNaver(String accessCode,String state, HttpServletResponse response) {
        log.info("AuthService - 네이버 인증 실행");

        // access_token 요청
        String accessToken = naverUtil.requestToken(accessCode, state);

        // 사용자 정보 요청
        NaverDTO.NaverUser naverUser = naverUtil.requestProfile(accessToken);

        // provider && providerId
        String provider = "naver";
        String providerId = String.valueOf(naverUser.getId());

        return handleSocialLogin(provider, providerId, response);
    }

    public LoginResponseDTO oAuthLoginWithGithub(String accessCode,String state, HttpServletResponse response) {
        log.info("AuthService - 깃허브 인증 실행");

        // access_token 요청
        String accessToken = gitHubUtil.requestToken(accessCode);

        // 사용자 정보 요청
        GitHubDTO.GithubUser githubUser = gitHubUtil.requestProfile(accessToken);

        // provider && providerId
        String provider = "github";
        String providerId = String.valueOf(githubUser.getId());

        return handleSocialLogin(provider, providerId, response);
    }

    public LoginResponseDTO oAuthLoginWithGoogle(String accessCode,String state, HttpServletResponse response) {
        log.info("AuthService - 구글 인증 실행");

        // access_token 요청
        String accessToken = googleUtil.requestToken(accessCode);

        // 사용자 정보 요청
        GoogleDTO.GoogleUser googleUser = googleUtil.requestProfile(accessToken);

        // provider && providerId
        String provider = "google";
        String providerId = String.valueOf(googleUser.getId());

        return handleSocialLogin(provider, providerId, response);
    }

    public LoginResponseDTO handleSocialLogin(String provider, String providerId, HttpServletResponse response) {
        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(provider, providerId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String userId = user.getId().toString();

            String accessToken = jwtUtil.createAccessToken(userId, user.getNickname(), user.getProvider());
            String refreshToken = jwtUtil.createRefreshToken(userId);
            // 기존 RT가 있으면 삭제 후 새로 저장 (중복 로그인 방지)
            refreshTokenRepository.delete(userId);
            refreshTokenRepository.save(userId, refreshToken, refreshTokenExpTime);
            log.info("Redis에 RefreshToken 저장 완료: userId={}, token={}", userId, refreshToken);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            // TODO: 운영 환경에서는 프로필에 따라 Secure=true 설정을 활성화해야 합니다.
            // if (isProductionProfile) {
            //   refreshTokenCookie.setSecure(true);
            // }
            refreshTokenCookie.setMaxAge((int) refreshTokenExpTime);
            response.addCookie(refreshTokenCookie);

            long nowMills = System.currentTimeMillis() + tokenExpireTime * 1000L;
            return new LoginResponseDTO(accessToken, nowMills, user.getNickname());
        } else {
            throw new AdditionalSignupRequiredException(provider, providerId);
        }
    }

    public boolean validateRefreshToken(String token) {
        return jwtUtil.validateRefreshToken(token) && !jwtUtil.isExpired(token);
    }

    public String getUserIdFromRefreshToken(String token) {
        if (!validateRefreshToken(token)) {
            throw new RuntimeException("유효하지 않은 Refresh Token");
        }
        return jwtUtil.getUserId(token);
    }

    public String getStoredRefreshToken(String userId) {
        return refreshTokenRepository.findByUserId(userId);
    }

    public Map<String, Object> issueNewAccessToken(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        String newAccessToken = jwtUtil.createAccessToken(user.getId().toString(), user.getNickname(), user.getProvider());
        long nowMills = System.currentTimeMillis() + tokenExpireTime * 1000L;

        return Map.of(
                "token", newAccessToken,
                "tokenExpire", nowMills);
    }


    public void oAuthcreateNewUser(AdditionalSignupRequest dto, HttpServletResponse response) {
        log.info("AuthService - 회원가입 실행");

        String provider = dto.getProvider();
        String providerId = dto.getProviderId();
        String name = dto.getName();
        String nickName = dto.getNickname();
        String birthday = dto.getBirthday();
        boolean agreement = dto.isAgreement();


        // 회원가입 사용자 user로 build
        User newUser = User.builder()
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .nickname(nickName)
                .birthday(birthday)
                .agreement(agreement)
                .build();

        // 닉네임 중복 검사
        userService.validateDuplicateNickName(newUser);

        // user 저장
        userRepository.save(newUser);
        log.info(newUser.toString());
    }

    @Transactional
    public void logout(String refreshToken) {
        // 1. Refresh Token에서 userId 추출
        String userId = getUserIdFromRefreshToken(refreshToken);
        log.info("로그아웃 요청: userId = {}", userId);

        // 2. Redis에서 Refresh Token 삭제
        refreshTokenRepository.delete(userId);
        log.info("Redis에서 Refresh Token 삭제 완료");
    }

}
