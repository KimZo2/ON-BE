package com.KimZo2.Back.global.security.handler;

import com.KimZo2.Back.domain.auth.info.CustomOAuth2User;
import com.KimZo2.Back.domain.auth.repository.RefreshTokenRepository;
import com.KimZo2.Back.global.entity.Role;
import com.KimZo2.Back.global.jwt.JwtUtil;
import com.KimZo2.Back.global.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // 환경변수로 빼기
    private static final String FRONTEND_URI = "https://on-line-green.vercel.app";

    @Value("${jwt.access-token-validity-in-seconds}")
    private long tokenExpireTime;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Role role = oAuth2User.getMember().getRole();

        if (role == Role.GUEST) {

            String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_URI + "/signup")
                    .queryParam("memberId", oAuth2User.getMemberId())
                    .queryParam("status", "guest")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            String memberId = oAuth2User.getMemberId().toString();
            String nickname = oAuth2User.getMember().getNickname();
            String provider = oAuth2User.getMember().getProvider();

            String accessToken = jwtUtil.createAccessToken(memberId, nickname, provider);
            String refreshToken = jwtUtil.createRefreshToken(memberId);

            refreshTokenRepository.delete(memberId);
            refreshTokenRepository.save(memberId, refreshToken, refreshTokenExpTime);

            long nowMills = System.currentTimeMillis() + tokenExpireTime * 1000L;

            CookieUtil.addCookie(response, "refresh_token", refreshToken, refreshTokenExpTime);

            String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_URI + "/login-success")
                    .queryParam("accessToken", accessToken)
                    .queryParam("accessTokenExpire", nowMills)
                    .queryParam("nickname", nickname)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}
