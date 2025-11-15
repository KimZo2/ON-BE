package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.member.UserInfoResponseDTO;
import com.KimZo2.Back.security.model.CustomUserDetails;
import com.KimZo2.Back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
