package com.KimZo2.Back.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRequestDTO {
    String nickname;
    String content;
    long timestamp;
}
