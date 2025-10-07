package com.KimZo2.Back.repository.redis;

import com.KimZo2.Back.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class RoomStoreRepositoryImpl implements RoomStoreRepository {
    private static final Logger log = LoggerFactory.getLogger(RoomStoreRepositoryImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> createRoomLua;

    /**
     * 방 이름이 겹치지 않도록 잠금 역할을 하는 키
     * setIfAbsent = SETNX (=key가 없을 때만 세팅)
     */
    @Override
    public boolean lockRoomName(String roomName, String roomId, Duration roomTTL) {
        String key = KeyFactory.roomName(roomName);
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, roomId, roomTTL);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public void createRoomRuntime(UUID roomId, String roomName, boolean isPrivate, UUID creatorId, int max, int roomType, Duration ttl, long nowMs) {
        String roomIdStr = roomId.toString();

        // KEYS 리스트 준비
        List<String> keys = List.of(
                KeyFactory.roomMeta(roomId),       // KEYS[1]
                KeyFactory.roomMembers(roomId),    // KEYS[2]
                KeyFactory.roomPos(roomId),        // KEYS[3]
                KeyFactory.roomSeen(roomId),       // KEYS[4]
                KeyFactory.roomActive(),           // KEYS[5]
                KeyFactory.roomHot(),              // KEYS[6]
                KeyFactory.roomPublic()            // KEYS[7]
        );

        // ARGV 값들을 문자열로 준비
        String[] args = {
                roomIdStr,                         // ARGV[1]
                roomName,                          // ARGV[2]
                isPrivate ? "1" : "0",             // ARGV[3]
                creatorId.toString(),              // ARGV[4]
                String.valueOf(max),               // ARGV[5]
                String.valueOf(roomType),          // ARGV[6]
                String.valueOf(ttl.toSeconds()),   // ARGV[7]
                String.valueOf(nowMs)              // ARGV[8]
        };

        redisTemplate.execute(createRoomLua, keys, args);
    }

    @Override
    public void releaseNameLock(String roomName) {
        redisTemplate.delete(KeyFactory.roomName(roomName));
    }

    @Override
    public void deleteRoomRuntimeIfPresent(UUID roomId) {
        String key = KeyFactory.roomMeta(roomId);

        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            // 모니터링만 하도록
            log.warn("RedisRoomStore - Redis 런타임 삭제 실패 roomId={}", roomId, e);
        }
    }

    // String -> byte 변환기
    private byte[] b(String s){ return s.getBytes(StandardCharsets.UTF_8); }
}
