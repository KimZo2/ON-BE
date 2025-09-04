package com.KimZo2.Back.service.socket;

import com.KimZo2.Back.exception.ws.BadDestinationException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SubscribeGuard implements ChannelInterceptor {


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var acc = StompHeaderAccessor.wrap(message);
        if(acc.getCommand() == StompCommand.SUBSCRIBE) {
            String dest = acc.getDestination();
            Principal principal = acc.getUser();
            UUID roomId = extractRoomId(dest);

//            if (dest.startsWith("/topic/room.") && !roomStateManager.isMember(roomId, principal.getName())) {
//                throw new RoomAccessDeniedException("Not a member of room " + roomId);
//            }
        }
        return message;
    }

    // roomID 파싱 처리 로직
    private UUID extractRoomId(String dest) {
        Pattern pattern = Pattern.compile("^/topic/room\\.([^.]+)\\..+$");
        Matcher mathcer = pattern.matcher(dest);
        if (mathcer.matches()) {
            // roomId return
            return UUID.fromString(mathcer.group(1));
        }
        throw new BadDestinationException("잘못된 destination: " + dest);

    }
}
