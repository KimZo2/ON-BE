package com.KimZo2.Back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Configuration
public class RedisScriptConfig {

    @Bean
    public DefaultRedisScript<Long> createRoomLua() {
        String script = """
            -- KEYS:
            -- 1: rooms:{roomId}            (메타 데이터 Hash)
            -- 2: rooms:{roomId}:members    (멤버 Set)
            -- 3: rooms:{roomId}:pos        (위치 Hash)
            -- 4: rooms:{roomId}:seen       (마지막 활동 Sorted Set)
            -- 5: rooms:active_list         (전체 활성 방 Set)
            -- 6: rooms:hot                 (인기 방 Sorted Set)
            -- 7: rooms:public              (공개 방 Sorted Set)
            -- 8: rooms:notify:{roomId}     (10분 전 알림용 키)
            --
            -- ARGV:
            -- 1: roomId
            -- 2: roomName
            -- 3: visibility ("1" for private, "0" for public)
            -- 4: creatorId
            -- 5: maxPersonCnt
            -- 6: roomType (background img)
            -- 7: ttlSec
            -- 8: nowMs (timestamp)
            --
            -- 반환: 1 (성공), 0 (이미 존재하여 실패)

            local metaKey       = KEYS[1]
            local membersKey    = KEYS[2]
            local posKey        = KEYS[3]
            local seenKey       = KEYS[4]
            local activeListKey = KEYS[5]
            local hotKey        = KEYS[6]
            local publicIdxKey  = KEYS[7]
            local notifyKey     = KEYS[8]

            local roomId        = ARGV[1]
            local roomName      = ARGV[2]
            local visibility    = ARGV[3]
            local creatorId     = ARGV[4]
            local maxPerson     = ARGV[5]
            local roomType      = ARGV[6]
            local ttl           = tonumber(ARGV[7])
            local nowMs         = tonumber(ARGV[8])

            -- 1. 방이 이미 존재하는지 확인하여 중복 생성 방지
            if redis.call('EXISTS', metaKey) == 1 then
                return 0
            end

            -- 2. 방 메타 데이터 생성
            redis.call('HSET', metaKey,
                'roomName', roomName,
                'visibility', visibility,
                'roomBackgroundImg', roomType,
                'roomMaximumPersonCnt', maxPerson,
                'roomCurrentPersonCnt', 0,
                'peak', 0,
                'creatorId', creatorId,
                'createdAtMs', nowMs,
                'lastActiveMs', nowMs,
                'ttlSec', ttl,
                'state', 'CREATED',
                'active', 'true'
            )

            -- 3. 데이터 정합성을 위해 관련된 모든 키에 동일한 TTL 설정
            redis.call('SADD', membersKey, 'init')
            redis.call('SREM', membersKey, 'init')
    
            -- posKey(Hash)와 seenKey(Sorted Set)를 빈 상태로 생성
            redis.call('HSET', posKey, 'init', '1')
            redis.call('HDEL', posKey, 'init')
            redis.call('ZADD', seenKey, 0, 'init')
            redis.call('ZREM', seenKey, 'init')
    
            -- 이제 모든 키가 존재하므로 EXPIRE가 성공함
            redis.call('EXPIRE', metaKey, ttl)
            redis.call('EXPIRE', membersKey, ttl)
            redis.call('EXPIRE', posKey, ttl)
            redis.call('EXPIRE', seenKey, ttl)

            -- 4. 전체 방 목록 및 인덱스에 추가
            redis.call('SADD', activeListKey, roomId)
            redis.call('ZADD', hotKey, nowMs, roomId)

            if visibility == '0' then -- 공개 방일 경우
                redis.call('ZADD', publicIdxKey, nowMs, roomId)
            end
            
            -- 방 종료 10분전 알림 설정
            local notifyTtl = ttl - 600;
            if notifyTtl > 0 then
                redis.call('SET', notifyKey, '1')
                redis.call('EXPIRE', notifyKey, notifyTtl)
            end  

            return 1
            """;
        DefaultRedisScript<Long> lua = new DefaultRedisScript<>();
        lua.setScriptText(script);
        lua.setResultType(Long.class);
        return lua;
    }

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
            -- 6) rooms:{roomId}:nicknames     (닉네임 해시)
            --
            -- ARGV:
            -- 1) userId
            -- 2) nickname
            -- 3) presenceTtlSec
            -- 4) userRoomTtlSec
            -- 5) nowMs
            --
            -- 반환: {code, count}
            -- code: 0 OK, 4 ALREADY, 3 FULL, 1 CLOSED_OR_NOT_FOUND

            local metaKey    = KEYS[1]
            local membersKey = KEYS[2]
            local userRoom   = KEYS[3]
            local presence   = KEYS[4]
            local hotKey     = KEYS[5]
            local nickKey    = KEYS[6]

            local uid        = ARGV[1]
            local nick       = ARGV[2]
            local pttl       = tonumber(ARGV[3])
            local urttl      = tonumber(ARGV[4])
            local nowMs      = tonumber(ARGV[5])

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
              local rid = string.match(metaKey, 'rooms:(.+)$')
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
            redis.call('HSET', nickKey, uid, nick)
            local newCur = redis.call('HINCRBY', metaKey, 'roomCurrentPersonCnt', 1)
            redis.call('SET', presence, '1', 'EX', pttl)
            local rid = string.match(metaKey, 'rooms:(.+)$')
            if rid then
                redis.call('SET', userRoom, rid, 'EX', urttl)
                redis.call('ZADD', hotKey, nowMs, rid)
            end

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
    -- 11: nickname

    local userId   = ARGV[1]
    local session  = ARGV[2]
    local x        = ARGV[3]
    local y        = ARGV[4]
    local ts       = tonumber(ARGV[5])
    local seq      = tonumber(ARGV[6])
    local ttl      = tonumber(ARGV[7])
    local roomId    = ARGV[8]
    local direction = ARGV[9]
    local isMoving  = ARGV[10] or "false"
    local nickname  = ARGV[11]
    

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
        -- old format: "nickname,x,y,ts,seq,direction,isMoving"
        local parts = {}
        for v in string.gmatch(old, '([^,]+)') do table.insert(parts, v) end
        local oldSeq = tonumber(parts[5]) or -1
        if seq <= oldSeq then
            -- stale; do not update pos, but still update seen/presence
            redis.call('ZADD', KEYS[4], ts, userId)
            if roomId and roomId ~= '' then
                redis.call('ZADD', KEYS[5], ts, roomId)
            end
            return {'STALE', oldSeq}
        end
    end

    local value = nickname .. ',' .. x .. ',' .. y .. ',' .. tostring(ts) .. ',' .. tostring(seq) .. ',' .. direction .. ',' .. isMoving
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

    // RedisConfig.java

    @Bean
    public DefaultRedisScript<Long> cleanupUserLua() {
        String script = """
        -- KEYS:
        -- 1: rooms:{roomId}:members       (멤버 SET)
        -- 2: rooms:{roomId}:pos           (위치 HASH)
        -- 3: rooms:{roomId}               (메타 HASH)
        -- 4: rooms:{roomId}:seen          (마지막 활동 Sorted Set)
        -- 5: users:{userId}:rooms         (유저-방 역참조 STRING)
        -- 6: rooms:{roomId}:nicknames     (닉네임 HASH)
        --
        -- ARGV:
        -- 1: userId
        --
        -- 반환: 1 (정리 성공), 0 (이미 없어서 정리 안함)

        local membersKey = KEYS[1]
        local posKey     = KEYS[2]
        local metaKey    = KEYS[3]
        local seenKey    = KEYS[4]
        local userRoomKey= KEYS[5]
        local nickKey    = KEYS[6]
        local uid        = ARGV[1]

        if redis.call('SREM', membersKey, uid) == 1 then
            -- 기존 정리 로직
            redis.call('HDEL', posKey, uid)
            redis.call('HDEL', nickKey, uid) 
            redis.call('HINCRBY', metaKey, 'roomCurrentPersonCnt', -1)
            redis.call('ZREM', seenKey, uid)
            
            -- 추가된 키 정리 로직
            redis.call('DEL', userRoomKey)
            -- redis.call('DEL', moveRateKey)

            return 1
        end

        return 0
    """;
        DefaultRedisScript<Long> lua = new DefaultRedisScript<>();
        lua.setScriptText(script);
        lua.setResultType(Long.class);
        return lua;
    }

    @Bean
    public DefaultRedisScript<Long> deleteRoomLua() {
        String script = """
                -- KEYS:
                -- 1: rooms:{roomId}
                -- 2: rooms:{roomId}:members
                -- 3: rooms:{roomId}:pos
                -- 4: rooms:{roomId}:seen
                -- 5: rooms:active_list
                -- 6: rooms:hot:zset
                -- 7: rooms:public:zset
                -- ARGV:
                -- 1: roomId

                -- Check if the room exists
                if redis.call('exists', KEYS[1]) == 0 then
                    return 0
                end

                -- Delete room-related data
                redis.call('del', KEYS[1]) -- room meta
                redis.call('del', KEYS[2]) -- room members
                redis.call('del', KEYS[3]) -- room positions
                redis.call('del', KEYS[4]) -- room seen users

                -- Remove room from sorted sets
                redis.call('srem', KEYS[5], ARGV[1]) -- active rooms
                redis.call('zrem', KEYS[6], ARGV[1]) -- hot rooms
                redis.call('zrem', KEYS[7], ARGV[1]) -- public rooms

                return 1
                """;
        DefaultRedisScript<Long> lua = new DefaultRedisScript<>();
        lua.setScriptText(script);
        lua.setResultType(Long.class);
        return lua;
    }
}
