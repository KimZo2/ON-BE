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
        socketService.joinRoom(roomId, userId, sessionId);
    }

    @MessageMapping("/room/{roomId}/leave")
    public void leaveRoom(@DestinationVariable UUID roomId,
                          Principal principal,
                          @Header("simpSessionId") String sessionId){
        UUID userId = UUID.fromString(principal.getName());

        socketService.leaveRoom(roomId, userId);
    }
}
