package com.KimZo2.Back.domain.auth.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserInfoResponseDTO {
    private UUID userId;
    private String nickname;
}
