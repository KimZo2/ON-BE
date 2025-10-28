package com.KimZo2.Back.exception.chat;

import com.KimZo2.Back.dto.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;

public class GlobalChatExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalChatExceptionHandler.class);

    @MessageExceptionHandler(InvalidChatRequestException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleInvalidChatRequest(InvalidChatRequestException e) {
        log.warn("Invalid Chat Request WebSocket exception: {}", e.getMessage());
        return new ErrorResponse("INVALID_CHAT_REQUEST", e.getMessage());
    }

    @MessageExceptionHandler(InvalidRoomIdException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleInvalidRoomId(InvalidRoomIdException e) {
        log.warn("Invalid Room ID for Chat WebSocket exception: {}", e.getMessage());
        return new ErrorResponse("INVALID_ROOM_ID", e.getMessage());
    }
}
