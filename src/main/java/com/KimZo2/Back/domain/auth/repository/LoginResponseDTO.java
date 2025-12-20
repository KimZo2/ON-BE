package com.KimZo2.Back.domain.auth.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private long tokenExpire;
    private String nickname;
}
