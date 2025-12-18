package com.KimZo2.Back.domain.auth.service;

import com.KimZo2.Back.domain.auth.dto.AdditionalSignupRequest;
import com.KimZo2.Back.domain.auth.dto.KakaoDTO;
import com.KimZo2.Back.domain.auth.repository.LoginResponseDTO;
import com.KimZo2.Back.domain.user.service.UserService;
import com.KimZo2.Back.global.entity.User;
import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.domain.auth.exception.AdditionalSignupRequiredException;
import com.KimZo2.Back.domain.user.repository.UserRepository;
import com.KimZo2.Back.domain.auth.repository.RefreshTokenRepository;
import com.KimZo2.Back.global.jwt.JwtUtil;
import com.KimZo2.Back.global.util.GitHubUtil;
import com.KimZo2.Back.global.util.GoogleUtil;
import com.KimZo2.Back.global.util.KakaoUtil;
import com.KimZo2.Back.global.util.NaverUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Map;
import java.util.UUID;

import static com.KimZo2.Back.global.exception.ErrorCode.USER_NOT_FOUND;

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
    private final GitHubUtil gitHubUtil;
    private final GoogleUtil googleUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // 카카오 로그인
    public LoginResponseDTO oAuthLoginWithKakao(String accessCode, HttpServletResponse response) {

        // access_token 요청
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);

        // 사용자 정보 요청
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        // provider && providerId
        String provider = "kakao";
        String providerId = String.valueOf(kakaoProfile.getId());

        return handleSocialLogin(provider, providerId, response);
    }

//    public LoginResponseDTO oAuthLoginWithNaver(String accessCode,String state, HttpServletResponse response) {
//
//        // access_token 요청
//        String accessToken = naverUtil.requestToken(accessCode, state);
//
//        // 사용자 정보 요청
//        NaverDTO.NaverUser naverUser = naverUtil.requestProfile(accessToken);
//
//        // provider && providerId
//        String provider = "naver";
//        String providerId = String.valueOf(naverUser.getId());
//
//        return handleSocialLogin(provider, providerId, response);
//    }
//
//    public LoginResponseDTO oAuthLoginWithGithub(String accessCode,String state, HttpServletResponse response) {
//        // access_token 요청
//        String accessToken = gitHubUtil.requestToken(accessCode);
//
//        // 사용자 정보 요청
//        GitHubDTO.GithubUser githubUser = gitHubUtil.requestProfile(accessToken);
//
//        // provider && providerId
//        String provider = "github";
//        String providerId = String.valueOf(githubUser.getId());
//
//        return handleSocialLogin(provider, providerId, response);
//    }
//
//    public LoginResponseDTO oAuthLoginWithGoogle(String accessCode,String state, HttpServletResponse response) {
//
//        // access_token 요청
//        String accessToken = googleUtil.requestToken(accessCode);
//
//        // 사용자 정보 요청
//        com.KimZo2.Back.domain.auth.dto.GoogleDTO.GoogleUser googleUser = googleUtil.requestProfile(accessToken);
//
//        // provider && providerId
//        String provider = "google";
//        String providerId = String.valueOf(googleUser.getId());
//
//        return handleSocialLogin(provider, providerId, response);
//    }

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
            // 회원 정보가 없어 추가 회원 정보를 받아야하는 경우
            throw new AdditionalSignupRequiredException(provider, providerId);
        }
    }

    public boolean validateRefreshToken(String token) {
        return jwtUtil.validateRefreshToken(token);
    }

    public String getUserIdFromRefreshToken(String token) {
        if (!validateRefreshToken(token)) {
            throw new RuntimeException("유효하지 않은 Refresh Token");
        }
        return jwtUtil.extractUserId(token, true);
    }

    //
    public String getStoredRefreshToken(String userId) {
        return refreshTokenRepository.findByUserId(userId);
    }

    // AccessToken 재발급
    public Map<String, Object> issueNewAccessToken(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String newAccessToken = jwtUtil.createAccessToken(user.getId().toString(), user.getNickname(), user.getProvider());
        long nowMills = System.currentTimeMillis() + tokenExpireTime * 1000L;

        return Map.of(
                "token", newAccessToken,
                "tokenExpire", nowMills);
    }


    @Transactional
    public String createNewUser(AdditionalSignupRequest dto, HttpServletResponse response) {

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

        return "회원가입이 완료되었습니다.";
    }

    @Transactional
    public String logout(String refreshToken) {
        // 1. Refresh Token에서 userId 추출
        String userId = getUserIdFromRefreshToken(refreshToken);
        log.info("로그아웃 요청: userId = {}", userId);

        // 2. Redis에서 Refresh Token 삭제
        refreshTokenRepository.delete(userId);
        log.info("Redis에서 Refresh Token 삭제 완료");

        return "로그아웃이 완료되었습니다.";
    }

}
