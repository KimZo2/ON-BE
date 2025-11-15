package com.KimZo2.Back.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private long tokenExpire;
    private String nickname;
}
