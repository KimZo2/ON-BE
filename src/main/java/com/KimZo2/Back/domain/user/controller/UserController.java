package com.KimZo2.Back.domain.user.controller;

import com.KimZo2.Back.domain.auth.repository.UserInfoResponseDTO;
import com.KimZo2.Back.domain.user.service.UserService;
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
    public UserInfoResponseDTO getUserInfo(@AuthenticationPrincipal String uuid) {
        UUID userId = UUID.fromString(uuid);
        return userService.getUserId(userId);
    }
}
