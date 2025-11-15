package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.chat.ChatRequestDTO;
import com.KimZo2.Back.service.ChatService;
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
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("room/{roomId}/chat")
    public void chatRequest(@DestinationVariable UUID roomId, Principal principal,
                            @Payload ChatRequestDTO dto){
        UUID userId = UUID.fromString(principal.getName());

        chatService.chatRequest(userId, roomId, dto);
    }
}
