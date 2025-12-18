package com.KimZo2.Back.domain.roomchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatResponseDTO {
    UUID userId;
    String nickname;
    String content;
    long timestamp;
}
