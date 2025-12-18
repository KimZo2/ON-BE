package com.KimZo2.Back.global.config;

import com.KimZo2.Back.domain.user.repository.MembersRepository;
import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SubscribeGuard implements ChannelInterceptor {

    private final MembersRepository membersRepository;

    private static final Pattern ROOM_TOPIC =
            Pattern.compile("^/topic/room\\.([^.]+)(?:\\..+)?$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            String dest = acc.getDestination();
            if (dest == null) throw new CustomException(ErrorCode.INVALID_DESTINATION);

            if (dest.startsWith("/user/queue/join")) {
                return message;
            }

            Matcher m = ROOM_TOPIC.matcher(dest);
            if (m.matches()) {
                Principal principal = acc.getUser();
                if (principal == null) {
                    throw new CustomException(ErrorCode.INVALID_DESTINATION);
                }

                UUID roomId = UUID.fromString(m.group(1));
                String userId = principal.getName();

                if (!membersRepository.isMember(roomId, userId)) {
                    throw new CustomException(ErrorCode.ROOM_ACCESS_DENIED);
                }
                return message;
            }
        }
        return message;
    }
}
