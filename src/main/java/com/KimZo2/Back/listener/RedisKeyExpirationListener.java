package com.KimZo2.Back.listener;

import com.KimZo2.Back.repository.redis.RoomCleanUpRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

class KeyFactory {
    public static final String ROOM_META_PREFIX = "rooms:";
    public static final String ROOM_NOTIFY_PREFIX = "rooms:notify:";
}

@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisKeyExpirationListener.class);

    private final RoomCleanUpRepository roomCleanUpRepository;
    private final SimpMessagingTemplate msg;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey == null) {
            return;
        }

        if (expiredKey.startsWith(KeyFactory.ROOM_NOTIFY_PREFIX)) {
            String roomIdStr = expiredKey.substring(KeyFactory.ROOM_NOTIFY_PREFIX.length());

            System.out.println("10분 전 알림 발송 (Room ID: " + roomIdStr + ")");

            // STOMP 토픽으로 "10분 뒤 만료" 알림 전송
            String topic = "/topic/room/" + roomIdStr + "/notification";
            msg.convertAndSend(topic, "10분 뒤 방이 만료될 예정입니다.");
        }
        else if (expiredKey.startsWith(KeyFactory.ROOM_META_PREFIX)) {
            String roomIdStr = expiredKey.substring(KeyFactory.ROOM_META_PREFIX.length());

            try {
                UUID roomId = UUID.fromString(roomIdStr);
                System.out.println("방 만료 처리 (Room ID: " + roomIdStr + ")");
                String topic = "/topic/room/" + roomIdStr + "/expiration";
                msg.convertAndSend(topic, "방이 만료되었습니다.");
                roomCleanUpRepository.deleteAllRoomData(roomId);
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 UUID 형식의 키가 만료된 경우
                log.warn("Invalid UUID format for expired key: {}", roomIdStr, e);
            }  catch (Exception e) {
                // Redis, Messaging 등 다른 모든 예외 처리
                log.error("Error processing expired key: {}", expiredKey, e);
            }
        }
    }
}
