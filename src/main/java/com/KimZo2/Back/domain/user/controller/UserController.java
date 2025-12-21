package com.KimZo2.Back.domain.user.controller;

import com.KimZo2.Back.domain.auth.repository.UserInfoResponseDTO;
import com.KimZo2.Back.domain.user.service.UserService;
import com.KimZo2.Back.global.dto.ApiResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ApiResponse<?> getUserInfo(@AuthenticationPrincipal String uuid) {
        UUID userId = UUID.fromString(uuid);
        UserInfoResponseDTO user = userService.getUserId(userId);
        return ApiResponse.onSuccess(user);
    }
}
