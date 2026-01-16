package com.KimZo2.Back.domain.member.controller;

import com.KimZo2.Back.domain.member.dto.MemberIdResponseDTO;
import com.KimZo2.Back.domain.member.dto.AvatarChangeRequestDTO;
import com.KimZo2.Back.domain.member.dto.MemberInformationResponseDTO;
import com.KimZo2.Back.domain.member.service.MemberService;
import com.KimZo2.Back.global.dto.ApiResponse;
import com.KimZo2.Back.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "회원 고유 ID 조회",
            description = "로그인된 사용자의 UUID(MemberId)를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberIdResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 만료 또는 없음)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @GetMapping("/info")
    public ApiResponse<?> getUserId(@Parameter(hidden = true) Principal principal) {
        UUID memberId = UUID.fromString(principal.getName());

        MemberIdResponseDTO member = memberService.getUserId(memberId);
        return ApiResponse.onSuccess(member);
    }

    @Operation(
            summary = "내 정보 조회",
            description = "로그인된 사용자의 상세 정보(닉네임, 아바타, 이메일 등)를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberInformationResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @GetMapping("/me")
    public ApiResponse<?> getUserInformation(@Parameter(hidden = true) Principal principal) {
        UUID memberId = UUID.fromString(principal.getName());

        MemberInformationResponseDTO member = memberService.getUserInformation(memberId);
        return ApiResponse.onSuccess(member);
    }

    @Operation(
            summary = "아바타 변경",
            description = "사용자의 아바타(int 번호)를 변경합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @PatchMapping("/change")
    public ApiResponse<?> changeAvatar(@Parameter(hidden = true) Principal principal,
                                       @RequestBody AvatarChangeRequestDTO dto) {
        UUID memberId = UUID.fromString(principal.getName());

        String message = memberService.changeAvatar(memberId, dto.getAvatar());

        return ApiResponse.onSuccess(message);
    }
}
