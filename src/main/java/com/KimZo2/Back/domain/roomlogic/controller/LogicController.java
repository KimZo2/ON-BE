package com.KimZo2.Back.domain.roomlogic.controller;

import com.KimZo2.Back.domain.roomlogic.dto.MoveCommand;
import com.KimZo2.Back.domain.roomlogic.dto.PingRequest;
import com.KimZo2.Back.domain.roomlogic.dto.Snapshot;
import com.KimZo2.Back.domain.roomlogic.service.LogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class LogicController {
    private final SimpMessagingTemplate msg;
    private final LogicService logicService;

    // 방 단위 Ping (Presence 유지)
    @MessageMapping("room/{roomId}/ping")
    public void userPing(@DestinationVariable UUID roomId, Principal principal,
                         @Header(name = "simpSessionId") String sessionId,
                         @Payload(required = false) PingRequest ping){
        UUID userId = UUID.fromString(principal.getName());
        long now = Instant.now().toEpochMilli();

        logicService.updateSession(roomId, userId, sessionId, now);
    }

    // 좌표 이동
    @MessageMapping("room/{roomId}/move")
    public void userCoordinate(@DestinationVariable UUID roomId, Principal principal,
                     @Header(name = "simpSessionId") String sessionId,
                     @Payload MoveCommand cmd) {
        UUID userId = UUID.fromString(principal.getName());

        // 검증 (형식/범위/NaN)

        // 좌표 업데이트
        var ack = logicService.updatePosition(roomId, userId, sessionId, cmd);

        msg.convertAndSendToUser(String.valueOf(userId), "/queue/move-ack", ack);
    }

    // 초기 스냅샷
    @MessageMapping("room/{roomId}/sync")
    public void userSYNC(@DestinationVariable UUID roomId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        Snapshot snapshot = logicService.loadSnapshot(roomId, userId);

        msg.convertAndSendToUser(String.valueOf(userId), "/queue/pos-snapshot", snapshot);
    }
}
