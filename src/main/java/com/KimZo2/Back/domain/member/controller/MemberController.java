package com.KimZo2.Back.domain.member.controller;

import com.KimZo2.Back.domain.auth.dto.MemberInfoResponseDTO;
import com.KimZo2.Back.domain.member.service.MemberService;
import com.KimZo2.Back.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/info")
    public ApiResponse<?> getUserInfo(@AuthenticationPrincipal String uuid) {
        UUID userId = UUID.fromString(uuid);
        MemberInfoResponseDTO member = memberService.getUserId(userId);
        return ApiResponse.onSuccess(member);
    }
}
