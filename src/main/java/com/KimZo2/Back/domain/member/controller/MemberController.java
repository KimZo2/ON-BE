package com.KimZo2.Back.domain.member.controller;

import com.KimZo2.Back.domain.member.dto.MemberIdResponseDTO;
import com.KimZo2.Back.domain.member.dto.AvatarChangeRequestDTO;
import com.KimZo2.Back.domain.member.dto.MemberInformationResponseDTO;
import com.KimZo2.Back.domain.member.service.MemberService;
import com.KimZo2.Back.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/info")
    public ApiResponse<?> getUserId(Principal principal) {
        UUID memberId = UUID.fromString(principal.getName());

        MemberIdResponseDTO member = memberService.getUserId(memberId);
        return ApiResponse.onSuccess(member);
    }

    @GetMapping("/me")
    public ApiResponse<?> getUserInformation(Principal principal) {
        UUID memberId = UUID.fromString(principal.getName());

        MemberInformationResponseDTO member = memberService.getUserInformation(memberId);
        return ApiResponse.onSuccess(member);
    }


    @PostMapping("/change")
    public ApiResponse<?> changeAvatar(AvatarChangeRequestDTO dto) {
        UUID memberId = UUID.fromString(dto.getMemberId());

        String message = memberService.changeAvatar(memberId, dto.getAvatar());

        return ApiResponse.onSuccess(message);
    }
}
