package com.KimZo2.Back.domain.roomchat.service;

import com.KimZo2.Back.domain.roomchat.dto.ChatRequestDTO;
import com.KimZo2.Back.domain.roomchat.dto.ChatResponseDTO;
import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate simpMessagingTemplate; // SimpMessagingTemplate 주입

    // 채팅 입력 시 topic 별로 broadcast하는 로직
    public void chatRequest(UUID userId, UUID roomId, ChatRequestDTO dto) {
        // 기본 유효성 검사
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }

        if (roomId == null) {
            throw new CustomException(ErrorCode.ROOM_ID_REQUIRED);
        }

        // ChatResponseDTO 생성
        ChatResponseDTO chatResponseDTO = new ChatResponseDTO(
                userId,
                dto.getNickname(),
                dto.getContent(),
                dto.getTimestamp()
        );

        // STOMP 토픽으로 메시지 브로드캐스트
        simpMessagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", chatResponseDTO);
    }
}
