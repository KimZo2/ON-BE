package com.KimZo2.Back.domain.roomchat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRequestDTO {
    @NotBlank
    String nickname;

    @NotBlank
    String content;

    @NotBlank
    long timestamp;
}
