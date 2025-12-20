package com.KimZo2.Back.global.config;

import com.KimZo2.Back.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker // 스프링이 WebSocket + STOMP 사용
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final SubscribeGuard subscribeGuard;

    /**
     * 소켓 연결 URL 등록 (endpoint)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("/*")
                .withSockJS();
    }

    /**
    * 메시지 브로커 설정
    * 클라이언트가 SUBSCRIBE 할 주소와
    * 클라이언트가 SEND 할 주소의 prefix 등을 설정
    * */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // 구독용 (전체, 사용자)
        config.enableSimpleBroker("/topic", "/queue");
        // 개인에게만 보내기 (/user/{username}/queue 자동 매핑)
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 클라이언트 → 서버로 들어오는 채널 설정 (Interceptor 등록)
     * */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthChannelInterceptor(jwtUtil));
        registration.interceptors(subscribeGuard);
    }
}
