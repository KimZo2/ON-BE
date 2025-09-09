package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.room.JoinResult;
import com.KimZo2.Back.dto.room.RoomEnterDTO;
import com.KimZo2.Back.dto.room.RoomEnterResponseDTO;
import com.KimZo2.Back.service.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SocketController {
    private final SimpMessagingTemplate msg;
    private final SocketService socketService;

    @MessageMapping("/room/{roomId}/join")
    public void joinRoom(@DestinationVariable UUID roomId,
                     @Payload RoomEnterDTO dto,
                     Principal principal,
                     @Header("simpSessionId") String sessionId){

        UUID userId = UUID.fromString(principal.getName());

        // 사용자 및 비밀번호 체크
        socketService.checkRoom(roomId, dto.getPassword());

        // 방 입장 로직
        JoinResult result = socketService.joinRoom(roomId, userId, sessionId);

        switch (result.status()) {
            case OK -> {
                // 브로드캐스트
                msg.convertAndSend("/topic/room." + roomId,
                        new RoomEnterResponseDTO(roomId, "JOIN", result.count()));
                // 개인 응답
                msg.convertAndSendToUser(userId.toString(), "/queue/join",
                        new RoomEnterResponseDTO(roomId, "JOIN", result.count()));
            }
            case ALREADY -> {
                msg.convertAndSendToUser(userId.toString(), "/queue/join",
                        new RoomEnterResponseDTO(roomId, "ALREADY", result.count()));
            }
            case FULL -> {
                msg.convertAndSendToUser(userId.toString(), "/queue/join",
                        new RoomEnterResponseDTO(roomId, "FULL", result.count()));
            }
            case CLOSED_OR_NOT_FOUND -> {
                msg.convertAndSendToUser(userId.toString(), "/queue/join",
                        new RoomEnterResponseDTO(roomId, "CLOSED_OR_NOT_FOUND", result.count()));
            }
            default -> {
                msg.convertAndSendToUser(userId.toString(), "/queue/join",
                        new RoomEnterResponseDTO(roomId, "ERROR", result.count()));
            }
        }
    }
}
