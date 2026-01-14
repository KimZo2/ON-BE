package com.KimZo2.Back.domain.auth.service;

import com.KimZo2.Back.domain.auth.dto.AdditionalSignupRequestDTO;
import com.KimZo2.Back.domain.auth.dto.TokenResponseDTO;
import com.KimZo2.Back.domain.member.repository.MemberRepository;
import com.KimZo2.Back.domain.member.service.MemberService;
import com.KimZo2.Back.global.entity.Member;
import com.KimZo2.Back.global.entity.Role;
import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.domain.auth.repository.RefreshTokenRepository;
import com.KimZo2.Back.global.exception.ErrorCode;
import com.KimZo2.Back.global.jwt.JwtUtil;
import com.KimZo2.Back.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import java.util.UUID;

import static com.KimZo2.Back.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Value("${jwt.access-token-validity-in-seconds}")
    private long tokenExpireTime;
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private int refreshTokenExpTime;

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // 추가 회원가입 정보 입력 -> 회원가입 완료
    @Transactional
    public String signup(AdditionalSignupRequestDTO dto) {

        // Member 정보 조회
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // Member Role 조회
        if (member.getRole() == Role.USER) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED);
        }

        // 닉네임 중복 검사
        memberService.validateDuplicateNickName(dto.getNickname());

        member.setName(dto.getName());
        member.setNickname(dto.getNickname());
        member.setBirthday(dto.getBirthday());
        member.setAgreement(dto.isAgreement());
        member.setAvatar(1);

        // GUEST -> USER 승격
        member.setRole(Role.USER);

        // user 저장
        memberRepository.save(member);

        return "회원가입이 완료되었습니다.";
    }


    // AccessToken 및 RefreshToken 재발급
    @Transactional
    public TokenResponseDTO reissue(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 Refresh Token 추출
        String refreshToken = CookieUtil.getCookie(request, "refresh_token")
                .map(Cookie::getValue)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        // 토큰 유효성 검사
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 토큰에서 memberId 추출
        String memberId = jwtUtil.extractMemberId(refreshToken, true);

        // Redis에 저장된 토큰 가져오기
        String storedToken = refreshTokenRepository.findByUserId(memberId);
        // Redis 토큰과 쿠키 토큰 비교
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Member 정보 조회
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        // 새 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(memberId, member.getNickname(), member.getProvider());
        String newRefreshToken = jwtUtil.createRefreshToken(memberId);

        // Redis 업데이트
        refreshTokenRepository.save(memberId, newRefreshToken, refreshTokenExpTime);

        long nowMills = System.currentTimeMillis() + tokenExpireTime * 1000L;

        // 쿠키 업데이트
        CookieUtil.addCookie(response, "refresh_token", newRefreshToken, refreshTokenExpTime);

        return new TokenResponseDTO(newAccessToken, nowMills);
    }

    // 로그아웃
    @Transactional
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // accessToken 꺼내오기
        String accessToken = jwtUtil.resolveToken(request);

        // accessToken 유효 확인
        if (accessToken == null || !jwtUtil.validateAccessToken(accessToken)) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        // Redis에 저장된 refreshToken 삭제
        String memberId = jwtUtil.extractMemberId(accessToken, false);
        if (refreshTokenRepository.findByUserId(memberId) != null) {
            refreshTokenRepository.delete(memberId);
        }

        // 쿠기 refreshToken 삭제
        CookieUtil.deleteCookie(request, response, "refresh_token");

        return "로그아웃이 완료되었습니다.";
    }
}
