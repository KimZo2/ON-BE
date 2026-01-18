package com.KimZo2.Back.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MemberIdResponseDTO {
    private UUID memberId;
    private String nickname;
}
