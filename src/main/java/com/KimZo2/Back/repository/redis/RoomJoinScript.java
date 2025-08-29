package com.KimZo2.Back.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomJoinScript {
    private final StringRedisTemplate rt;

    // KEYS[1]=roomKey, KEYS[2]=membersKey, ARGV[1]=userId, ARGV[2]=nowMs, ARGV[3]=publicZsetKey, ARGV[4]=joinableZsetKey, ARGV[5]=roomId
    private final DefaultRedisScript<Long> script = new DefaultRedisScript<>(
            """
            local room = KEYS[1]
            if redis.call('EXISTS', room) == 0 then return -1 end
    
            local cur = tonumber(redis.call('HGET', room, 'current') or '0')
            local max = tonumber(redis.call('HGET', room, 'max') or '0')
            if cur >= max then return -2 end
    
            -- members 추가 (멱등)
            redis.call('SADD', KEYS[2], ARGV[1])
    
            cur = tonumber(redis.call('HINCRBY', room, 'current', 1))
            local peak = tonumber(redis.call('HGET', room, 'peak') or '0')
            if cur > peak then redis.call('HSET', room, 'peak', tostring(cur)) end
    
            redis.call('HSET', room, 'lastActiveMs', ARGV[2])
    
            -- 공개방이면 목록 최신화 + joinable 업데이트
            if redis.call('HGET', room, 'private') == '0' then
              redis.call('ZADD', ARGV[3], tonumber(ARGV[2]), ARGV[5])
              if cur < max then
                redis.call('ZADD', ARGV[4], tonumber(ARGV[2]), ARGV[5])
              else
                redis.call('ZREM', ARGV[4], ARGV[5])
              end
            end
            return 1
            """,
            Long.class
    );

    public long tryJoin(UUID roomId, UUID userId, long nowMs) {
        String id = roomId.toString();
        return rt.execute(
                script,
                List.of("room:" + id, "room:" + id + ":members"),
                userId.toString(),
                String.valueOf(nowMs),
                "rooms:public",
                "rooms:joinable",
                id
        );
    }
}

