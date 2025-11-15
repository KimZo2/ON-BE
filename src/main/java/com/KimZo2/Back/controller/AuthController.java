package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.auth.AdditionalSignupRequest;
import com.KimZo2.Back.dto.member.LoginResponseDTO;
import com.KimZo2.Back.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Tag(name="Auth", description = "네이버, 카카오 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Operation(summary = "카카오 로그인", description = "카카오로 로그인 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "428", description = "회원정보 없음, 추가 정보 필요"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/login/kakao")
    public ResponseEntity<LoginResponseDTO> kakaoLogin(@RequestParam("code") String accessCode,
                                                       HttpServletResponse response)
    {
        log.info("AuthController - /login/kakao  -  실행");

        LoginResponseDTO user = authService.oAuthLoginWithKakao(accessCode, response);

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "네이버 로그인", description = "네이버로 로그인 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "428", description = "회원정보 없음, 추가 정보 필요"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/login/naver")
    public ResponseEntity<LoginResponseDTO> naverLogin(
            @RequestParam("code") String accessCode,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response)
    {
        log.info("AuthController - /login/naver  -  실행");

        LoginResponseDTO user = authService.oAuthLoginWithNaver(accessCode,state, response);

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "깃허브 로그인", description = "깃허브로 로그인 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "428", description = "회원정보 없음, 추가 정보 필요"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/login/github")
    public ResponseEntity<LoginResponseDTO> githubLogin(
            @RequestParam("code") String accessCode,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response
    )
    {
        log.info("AuthController - /login/github  -  실행");

        LoginResponseDTO user = authService.oAuthLoginWithGithub(accessCode, state, response);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "구글 로그인", description = "구글로 로그인 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "428", description = "회원정보 없음, 추가 정보 필요"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/login/google")
    public ResponseEntity<LoginResponseDTO> googleLogin(
            @RequestParam("code") String accessCode,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response
    )
    {
        log.info("AuthController - /login/google  -  실행");

        LoginResponseDTO user = authService.oAuthLoginWithGoogle(accessCode, state, response);
        return ResponseEntity.ok(user);
    }


    @Operation(summary = "회원가입", description = "필요한 정보를 더 받아 로그인 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "닉네임 중복 실패"),
            @ApiResponse(responseCode = "401", description = "회원가입 실패")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> OAuthSignup(@RequestBody AdditionalSignupRequest dto,
                                                      HttpServletResponse response) {
        // User 생성
        authService.oAuthcreateNewUser(dto, response);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "회원가입이 완료되었습니다."));
    }
}
