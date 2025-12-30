package com.KimZo2.Back.domain.auth.controller;

import com.KimZo2.Back.domain.auth.dto.AdditionalSignupRequestDTO;
import com.KimZo2.Back.domain.auth.dto.TokenResponseDTO;
import com.KimZo2.Back.global.dto.ApiResponse;
import com.KimZo2.Back.domain.auth.service.AuthService;
import com.KimZo2.Back.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@Tag(name="Auth", description = "네이버, 카카오 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);


    @Operation(
            summary = "추가 회원가입",
            description = "GUEST 회원의 추가 정보를 입력하여 USER로 승격합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 유효성 검증 실패 (예: '닉네임은 필수입니다.', '생년월일 형식이 올바르지 않습니다.')",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "닉네임 중복 (AUTH_003)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 형식 오류 (닉네임 누락 등)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @PostMapping("/signup")
    public ApiResponse<?> OAuthSignup(@RequestBody AdditionalSignupRequestDTO dto) {

        String message = authService.signup(dto);

        return ApiResponse.onSuccess(message);
    }


    @Operation(
            summary = "추가 회원가입",
            description = "GUEST 회원의 추가 정보를 입력(DTO)하여 USER로 승격시킵니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 누락, 유효성 검증 실패, 형식 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "닉네임 중복 (AUTH_003)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @GetMapping("/reissue")
    public ApiResponse<TokenResponseDTO> reissue(HttpServletRequest request, HttpServletResponse response) {

        TokenResponseDTO tokenDto = authService.reissue(request, response);

        return ApiResponse.onSuccess(tokenDto);
    }


    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 삭제하고 로그아웃 처리합니다.",
            security = @SecurityRequirement(name = "access-token") // Swagger 설정에 따른 보안 스키마 이름 적용 필요
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Refresh Token이 존재하지 않거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request, HttpServletResponse response) {

        String message = authService.logout(request, response);

        return ApiResponse.onSuccess(message);
    }

}
