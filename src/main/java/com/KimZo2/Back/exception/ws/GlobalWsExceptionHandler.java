package com.KimZo2.Back.exception.ws;

import com.KimZo2.Back.dto.error.ErrorResponse;
import com.KimZo2.Back.exception.chat.InvalidChatRequestException;
import com.KimZo2.Back.exception.chat.InvalidRoomIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalWsExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalWsExceptionHandler.class);

    @MessageExceptionHandler({
            RoomNotFoundOrExpiredException.class,
            RoomFullException.class,
            BadPasswordException.class,
    })

    @SendToUser("/queue/errors")
    public ErrorResponse handleRoomExceptions(RuntimeException e) {

        String code =
                (e instanceof RoomFullException) ? "ROOM_FULL" :
                        (e instanceof BadPasswordException) ? "BAD_PASSWORD" :
                                "ROOM_NOT_FOUND";
        log.warn("Room related WebSocket exception: {}", e.getMessage());
        return new ErrorResponse(code, e.getMessage());
    }

    @MessageExceptionHandler(BadDestinationException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleBadDestination(BadDestinationException e) {
        log.warn("Bad Destination WebSocket exception: {}", e.getMessage());
        return new ErrorResponse("INVALID_DEST", e.getMessage());
    }

    @MessageExceptionHandler(RoomAccessDeniedException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleRoomAccessDenied(RoomAccessDeniedException e) {
        log.warn("Room Access Denied WebSocket exception: {}", e.getMessage());
        return new ErrorResponse("INVALID_USER", e.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleOthers(Exception e) {
        log.error("Unhandled WebSocket exception: {}", e.getMessage(), e);
        return new ErrorResponse("ERROR", "알 수 없는 오류가 발생했습니다.");
    }

    @MessageExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleAccessDenied(AccessDeniedException  e) {
        log.warn("Access Denied WebSocket exception: {}", e.getMessage());
        return new ErrorResponse("UNAUTHENTICATED_USER", e.getMessage());
    }

}

