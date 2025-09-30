package com.KimZo2.Back.repository.redis;

import com.KimZo2.Back.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class RoomStoreRepositoryImpl implements RoomStoreRepository {
    private static final Logger log = LoggerFactory.getLogger(RoomStoreRepositoryImpl.class);


    private final StringRedisTemplate redisTemplate;

    /**
     * 방 이름이 겹치지 않도록 잠금 역할을 하는 키
     * setIfAbsent = SETNX (=key가 없을 때만 세팅)
     */
    @Override
    public boolean lockRoomName(String roomName, String roomId, Duration roomTTL) {
        String key = "roomName:" + roomName;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, roomId, roomTTL);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public void createRoomRuntime(UUID roomId, String roomName, boolean isPrivate, UUID creatorId, int max, int roomType, Duration ttl, long nowMs) {
        String id = roomId.toString();

        redisTemplate.executePipelined((RedisCallback<Object>) con -> {
            String base = "rooms:" + id;

            Map<byte[], byte[]> map = new HashMap<>();
            map.put(b("roomName"), b(roomName));
            map.put(b("visibility"), b(isPrivate ? "1" : "0"));
            map.put(b("roomBackgroundImg"), b(String.valueOf(roomType)));
            map.put(b("roomMaximumPersonCnt"), b(String.valueOf(max)));
            map.put(b("roomCurrentPersonCnt"), b("0"));
            map.put(b("peak"), b("0"));
            map.put(b("creatorId"), b(creatorId.toString()));
            map.put(b("createdAtMs"), b(String.valueOf(nowMs)));
            map.put(b("lastActiveMs"), b(String.valueOf(nowMs)));
            map.put(b("ttlSec"), b(String.valueOf(ttl.toSeconds())));
            map.put(b("state"), b("CREATED"));
            map.put(b("active"), b("true"));
            con.hMSet(b(base), map);

            // 방장 기능 추가?
//            con.sAdd(b(base + ":members"), b(creatorId.toString()));

            con.expire(b(base), ttl.toSeconds());

            con.zAdd(b("rooms:hot"),nowMs, b(id));

            // 방 목록 조회 인덱스 생성
            if (!isPrivate) {
                con.zAdd(b("rooms:public"), nowMs, b(id));
            }

            return null;
        });
    }

    @Override
    public void releaseNameLock(String roomName) {
        redisTemplate.delete("roomName:" + roomName);
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
