package com.KimZo2.Back.service;

import com.KimZo2.Back.dto.chat.ChatRequestDTO;
import com.KimZo2.Back.dto.chat.ChatResponseDTO;
import com.KimZo2.Back.exception.chat.InvalidChatRequestException;
import com.KimZo2.Back.exception.chat.InvalidRoomIdException;
import com.KimZo2.Back.repository.redis.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate simpMessagingTemplate; // SimpMessagingTemplate 주입

    // 채팅 입력 시 topic 별로 broadcast하는 로직
    public void chatRequest(UUID userId, UUID roomId, ChatRequestDTO dto) {
        // 기본 유효성 검사
        if (userId == null) {
            throw new InvalidChatRequestException("User ID must not be null");
        }
        if (roomId == null) {
            throw new InvalidRoomIdException("Room ID must not be empty");
        }
        if (!StringUtils.hasText(dto.getNickname())) {
            throw new InvalidChatRequestException("Nickname must not be empty");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new InvalidChatRequestException("Chat content must not be empty");
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
