package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.room.RoomEnterDTO;
import com.KimZo2.Back.dto.room.RoomEnterResponseDTO;
import com.KimZo2.Back.service.socket.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
    public void join(@DestinationVariable UUID roomId, @Payload RoomEnterDTO dto, Principal principal){

        socketService.checkRoom(roomId, principal.getName(), dto.getPassword());

        // 채널에 구독하고 있는 사용자 중 특정 사용자에게 메시지 전송
        // /user/queue 는 Principal 클라에게만
        msg.convertAndSendToUser(principal.getName(), "/queue/join", new RoomEnterResponseDTO(roomId, "JOIN_OK"));
    }

}
