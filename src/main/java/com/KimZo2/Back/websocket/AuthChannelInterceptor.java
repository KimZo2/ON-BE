package com.KimZo2.Back.websocket;

import com.KimZo2.Back.security.jwt.JwtUtil;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.List;

/**
 * JWT 인증
* */
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public AuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
    * WebSocket 연결 시, 클라가 보낸 JWT 토큰을 꺼내,
    * Spring Security 인증객체로 바꾸는 작업
    * */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 헤더 읽기 가능
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP 프레임 명령어 중 CONNECT 처리 -> 처음 WebSocket 연결 시 만 동작
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // username 추출
            String username = jwtUtil.getUserId(token);
            accessor.setUser(
                    new UsernamePasswordAuthenticationToken(username, null, List.of())
            );
        }
        return message;
    }
}
