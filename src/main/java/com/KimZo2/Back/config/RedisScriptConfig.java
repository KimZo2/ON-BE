package com.KimZo2.Back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class RedisScriptConfig {

    @Bean
    public DefaultRedisScript<List> joinRoomLua() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        script.setScriptText("""
            -- KEYS:
            -- 1) rooms:{roomId}               (메타 해시)
            -- 2) rooms:{roomId}:members       (멤버 SET)
            -- 3) users:{userId}:rooms          (유저 역참조 STRING)
            -- 4) presence:{roomId}:{userId}:{sessionId}  (프레즌스 STRING)
            -- 5) rooms:hot
            --
            -- ARGV:
            -- 1) userId
            -- 2) presenceTtlSec
            -- 3) userRoomTtlSec
            -- 4) nowMs
            --
            -- 반환: {code, count}
            -- code: 0 OK, 4 ALREADY, 3 FULL, 1 CLOSED_OR_NOT_FOUND

            local metaKey    = KEYS[1]
            local membersKey = KEYS[2]
            local userRoom   = KEYS[3]
            local presence   = KEYS[4]
            local hotKey     = KEYS[5] 

            local uid        = ARGV[1]
            local pttl       = tonumber(ARGV[2])
            local urttl      = tonumber(ARGV[3])
            local nowMs      = tonumber(ARGV[4])

            -- 방 존재/활성 확인
            if redis.call('EXISTS', metaKey) == 0 then
              return {1, 0}
            end
            local active = redis.call('HGET', metaKey, 'active')
            if (not active) or active ~= 'true' then
              return {1, 0}
            end

            -- 이미 멤버면 TTL만 갱신
            if redis.call('SISMEMBER', membersKey, uid) == 1 then
              redis.call('SET', presence, '1', 'EX', pttl)
              local rid = string.match(metaKey, 'room:(.+)$')
              if rid then
                redis.call('SET', userRoom, rid, 'EX', urttl)
                redis.call('ZADD', hotKey, nowMs, rid)
              end
              local cur = tonumber(redis.call('HGET', metaKey, 'roomCurrentPersonCnt') or '0')
              return {4, cur}
            end

            -- 정원 체크
            local max = tonumber(redis.call('HGET', metaKey, 'roomMaximumPersonCnt') or '0')
            local cur = tonumber(redis.call('HGET', metaKey, 'roomCurrentPersonCnt') or '0')
            if max > 0 and cur >= max then
              return {3, cur}
            end

            -- 입장 처리 (원자)
            redis.call('SADD', membersKey, uid)
            redis.call('HINCRBY', metaKey, 'roomCurrentPersonCnt', 1)
            redis.call('SET', presence, '1', 'EX', pttl)
            local rid = string.match(metaKey, 'room:(.+)$')
            if rid then
                redis.call('SET', userRoom, rid, 'EX', urttl)
                redis.call('ZADD', hotKey, nowMs, rid)
            end
            local newCur = tonumber(redis.call('HGET', metaKey, 'roomCurrentPersonCnt') or '0')

            -- 피크 업데이트(옵션) - 방 동시 접속 최대치
            -- local peak = tonumber(redis.call('HGET', metaKey, 'peak') or '0')
            -- if newCur > peak then redis.call('HSET', metaKey, 'peak', tostring(newCur)) end

            return {0, newCur}
        """);
        return script;
    }

    @Bean
    public DefaultRedisScript<List> userMoveLua() {
        String script = """
    -- KEYS:
    -- 1: kMembers
    -- 2: kPresence
    -- 3: kPos
    -- 4: kSeen
    -- 5: kHot

    -- ARGV:
    -- 1: userId
    -- 2: sessionId
    -- 3: x
    -- 4: y
    -- 5: ts (server millis)
    -- 6: seq (client monotonic)
    -- 7: presenceTtlSec
    -- 8: roomId
    -- 9: direction
    -- 10: isMoving

    local userId   = ARGV[1]
    local session  = ARGV[2]
    local x        = ARGV[3]
    local y        = ARGV[4]
    local ts       = tonumber(ARGV[5])
    local seq      = tonumber(ARGV[6])
    local ttl      = tonumber(ARGV[7])
    local direction = ARGV[9]
    local isMoving  = ARGV[10] or "false"
    

    -- 1) member check
    if redis.call('SISMEMBER', KEYS[1], userId) == 0 then
        return {'NOT_MEMBER', 0}
    end

    -- 2) update presence
    redis.call('SET', KEYS[2], '1', 'EX', ttl)

    -- 3) upsert pos with seq freshness
    local field = userId
    local old = redis.call('HGET', KEYS[3], field)
    if old then
        -- old format: "x,y,ts,seq,direction,isMoving"
        local parts = {}
        for v in string.gmatch(old, '([^,]+)') do table.insert(parts, v) end
        local oldSeq = tonumber(parts[4]) or -1
        if seq <= oldSeq then
            -- stale; do not update pos, but still update seen/presence
            redis.call('ZADD', KEYS[4], ts, userId)
            if roomId and roomId ~= '' then
                redis.call('ZADD', KEYS[5], ts, roomId)
            end
            return {'STALE', oldSeq}
        end
    end

    local value = x .. ',' .. y .. ',' .. tostring(ts) .. ',' .. tostring(seq) .. ',' .. direction .. ',' .. isMoving
    redis.call('HSET', KEYS[3], field, value)

    -- 4) update seen
    redis.call('ZADD', KEYS[4], ts, userId)
    
    -- 5) update hot
    if roomId and roomId ~= '' then
        redis.call('ZADD', KEYS[5], ts, roomId)
    end

    return {'OK', seq}
    """;
        DefaultRedisScript<List> lua = new DefaultRedisScript<>();
        lua.setScriptText(script);
        lua.setResultType(List.class);
        return lua;
    }


}
