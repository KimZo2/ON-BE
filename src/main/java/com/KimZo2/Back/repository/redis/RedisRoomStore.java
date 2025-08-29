package com.KimZo2.Back.repository.redis;

import lombok.RequiredArgsConstructor;
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
public class RedisRoomStore {
    private final StringRedisTemplate redisTemplate;

    /**
     * 방 이름이 겹치지 않도록 잠금 역할을 하는 키
     * setIfAbsent = SETNX (=key가 없을 때만 세팅)
     */
    public boolean lockRoomName(String roomName, String roomId, Duration roomTTL) {
        String key = "roomname:" + roomName;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, roomId, roomTTL);
        return Boolean.TRUE.equals(ok);
    }

    public void createRoomRuntime(UUID roomId, String roomName, boolean isPrivate, UUID creatorId, int max, int roomType, Duration ttl, long nowMs) {
        String id = roomId.toString();

        redisTemplate.executePipelined((RedisCallback<Object>) con -> {
            String base = "room:" + id;

            Map<byte[], byte[]> map = new HashMap<>();
            map.put(b("name"), b(roomName));
            map.put(b("private"), b(isPrivate ? "1" : "0"));
            map.put(b("type"), b(String.valueOf(roomType)));
            map.put(b("max"), b(String.valueOf(max)));
            map.put(b("current"), b("0"));
            map.put(b("peak"), b("0"));
            map.put(b("creatorId"), b(creatorId.toString()));
            map.put(b("createdAtMs"), b(String.valueOf(nowMs)));
            map.put(b("lastActiveMs"), b(String.valueOf(nowMs)));
            map.put(b("ttlSec"), b(String.valueOf(ttl.toSeconds())));
            map.put(b("state"), b("CREATED"));
            con.hMSet(b(base), map);

            // 방장 기능 추가?
//            con.sAdd(b(base + ":members"), b(creatorId.toString()));

            con.expire(b(base), ttl.toSeconds());

            // 방 목록 조회 인덱스 생성
            if (!isPrivate) {
                con.zAdd(b("rooms:public"), nowMs, b(id));
            }

            return null;
        });
    }

    public void releaseNameLock(String roomName) {
        redisTemplate.delete("roomname:" + roomName);
    }


    // String -> byte 변환기
    private byte[] b(String s){ return s.getBytes(StandardCharsets.UTF_8); }


}
