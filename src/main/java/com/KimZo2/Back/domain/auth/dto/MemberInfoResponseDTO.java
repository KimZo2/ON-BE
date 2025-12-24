package com.KimZo2.Back.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MemberInfoResponseDTO {
    private UUID memberId;
    private String nickname;
}
