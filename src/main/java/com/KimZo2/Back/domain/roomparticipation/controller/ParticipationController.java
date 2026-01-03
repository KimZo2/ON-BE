package com.KimZo2.Back.domain.roomparticipation.controller;

import com.KimZo2.Back.domain.room.dto.RoomEnterDTO;
import com.KimZo2.Back.domain.roomparticipation.service.ParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ParticipationController {
    private final ParticipationService participationServiceService;

    @MessageMapping("/room/{roomId}/join")
    public void joinRoom(@DestinationVariable UUID roomId,
                         @Payload RoomEnterDTO dto,
                         Principal principal,
                         @Header("simpSessionId") String sessionId){

        UUID memberId = UUID.fromString(principal.getName());

        // 사용자 및 비밀번호 체크
        participationServiceService.checkRoom(roomId, dto.getPassword());

        // 방 입장 로직
        participationServiceService.joinRoom(roomId, memberId, sessionId);
    }

    @MessageMapping("/room/{roomId}/leave")
    public void leaveRoom(@DestinationVariable UUID roomId,
                          Principal principal,
                          @Header("simpSessionId") String sessionId){
        UUID userId = UUID.fromString(principal.getName());

        participationServiceService.leaveRoom(roomId, userId);
    }
}
