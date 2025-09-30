package com.KimZo2.Back.exception.ws;

import com.KimZo2.Back.dto.error.ErrorResponse;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalWsExceptionHandler {

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
        return new ErrorResponse(code, e.getMessage());
    }

    @MessageExceptionHandler(BadDestinationException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleBadDestination(BadDestinationException e) {
        return new ErrorResponse("INVALID_DEST", e.getMessage());
    }

    @MessageExceptionHandler(RoomAccessDeniedException.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleRoomAccessDenied(RoomAccessDeniedException e) {
        return new ErrorResponse("INVALID_USER", e.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorResponse handleOthers(Exception e) {
        return new ErrorResponse("ERROR", "알 수 없는 오류가 발생했습니다.");
    }


    @MessageExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleAccessDenied(RoomAccessDeniedException e) {
        return new ErrorResponse("UNAUTHENTICATED_USER", e.getMessage());
    }

}

