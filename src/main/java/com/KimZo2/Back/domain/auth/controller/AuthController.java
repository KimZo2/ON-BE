package com.KimZo2.Back.domain.auth.controller;

import com.KimZo2.Back.domain.auth.dto.AdditionalSignupRequest;
import com.KimZo2.Back.global.dto.ApiResponse;
import com.KimZo2.Back.domain.auth.repository.LoginResponseDTO;
import com.KimZo2.Back.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Tag(name="Auth", description = "네이버, 카카오 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 인증 서버에서 발급받은 **인가 코드(code)**로 로그인을 수행합니다.<br>" +
                    "기존 회원이면 **Access Token**을 발급하고, " +
                    "신규 회원이거나 추가 정보가 필요하면 **428 에러**를 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 (토큰 발급)",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "428",
                    description = "추가 회원가입 정보 필요 (AUTH_001)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 코드 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/login/kakao")
    public ApiResponse<LoginResponseDTO> kakaoLogin(@RequestParam("code") String accessCode,
                                                       HttpServletResponse response)
    {
        log.info("AuthController - /login/kakao  -  실행");

        LoginResponseDTO user = authService.oAuthLoginWithKakao(accessCode, response);

        return ApiResponse.onSuccess(user);
    }

//    @Operation(summary = "네이버 로그인", description = "네이버로 로그인 합니다.")
//    @ApiResponses({
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "428", description = "회원정보 없음, 추가 정보 필요"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
//    })
//    @GetMapping("/login/naver")
//    public ApiResponse<LoginResponseDTO> naverLogin(
//            @RequestParam("code") String accessCode,
//            @RequestParam(value = "state", required = false) String state,
//            HttpServletResponse response)
//    {
//        log.info("AuthController - /login/naver  -  실행");
//
//        LoginResponseDTO user = authService.oAuthLoginWithNaver(accessCode,state, response);
//
//        return ApiResponse.onSuccess(user);
//    }
//
//    @Operation(summary = "깃허브 로그인", description = "깃허브로 로그인 합니다.")
//    @ApiResponses({
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "428", description = "회원정보 없음, 추가 정보 필요"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
//    })
//    @GetMapping("/login/github")
//    public ResponseEntity<LoginResponseDTO> githubLogin(
//            @RequestParam("code") String accessCode,
//            @RequestParam(value = "state", required = false) String state,
//            HttpServletResponse response
//    )
//    {
//        log.info("AuthController - /login/github  -  실행");
//
//        LoginResponseDTO user = authService.oAuthLoginWithGithub(accessCode, state, response);
//        return ResponseEntity.ok(user);
//    }
//
//    @Operation(summary = "구글 로그인", description = "구글로 로그인 합니다.")
//    @ApiResponses({
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "428", description = "회원정보 없음, 추가 정보 필요"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
//    })
//    @GetMapping("/login/google")
//    public ResponseEntity<LoginResponseDTO> googleLogin(
//            @RequestParam("code") String accessCode,
//            @RequestParam(value = "state", required = false) String state,
//            HttpServletResponse response
//    )
//    {
//        log.info("AuthController - /login/google  -  실행");
//
//        LoginResponseDTO user = authService.oAuthLoginWithGoogle(accessCode, state, response);
//        return ResponseEntity.ok(user);
//    }


    @Operation(summary = "회원가입", description = "필요한 정보를 더 받아 로그인 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "닉네임 중복 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "회원가입 실패")
    })
    @PostMapping("/signup")
    public ApiResponse<?> OAuthSignup(@RequestBody AdditionalSignupRequest dto,
                                                      HttpServletResponse response) {
        // User 생성
        String message = authService.createNewUser(dto, response);

        return ApiResponse.onSuccess(message);
    }


    @Operation(summary = "Access Token 재발급", description = "만료된 Access Token을 Refresh Token으로 갱신합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Access Token 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh Token 만료 또는 유효하지 않음 → 재로그인 필요")
    })
    @GetMapping("/refresh")
    public ApiResponse<?> refreshToken(HttpServletResponse response,
                                          @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null || !authService.validateRefreshToken(refreshToken)) {
            log.warn("Refresh Token 없음 또는 유효하지 않음");
            return ApiResponse.onFailure("401", "재로그인이 필요합니다.");
        }

        // 새 Access Token 발급
        String userId = authService.getUserIdFromRefreshToken(refreshToken);

        // Redis에 저장된 refresh token과 비교
        String storedToken = authService.getStoredRefreshToken(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("Redis에 저장된 Refresh Token과 불일치 → 재로그인 필요");
            return ApiResponse.onFailure("401", "재로그인이 필요합니다.");
        }

        log.info("Redis에 저장된 Refresh Token과 " + true);
        Map<String, Object> accessTokenData = authService.issueNewAccessToken(userId);

        return ApiResponse.onSuccess(accessTokenData);
    }


    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 로그아웃합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh Token이 없음")
    })
    @PostMapping("/logout")
    public ApiResponse<?> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            log.warn("Refresh Token이 쿠키에 없음");
            return ApiResponse.onFailure("400", "Refresh Token이 없습니다.");
        }

        String message = authService.logout(refreshToken);

        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setPath("/");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setMaxAge(0); // 즉시 만료
        response.addCookie(deleteCookie);

        return ApiResponse.onSuccess(message);
    }

}
