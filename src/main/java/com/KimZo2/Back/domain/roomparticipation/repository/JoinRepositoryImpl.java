package com.KimZo2.Back.domain.roomparticipation.repository;

import com.KimZo2.Back.global.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.KimZo2.Back.domain.room.dto.JoinResult;
import com.KimZo2.Back.domain.room.dto.JoinResult.JoinStatus;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class JoinRepositoryImpl implements JoinRepository {
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> joinRoomLua;

    @Override
    public JoinResult join(UUID roomId, UUID userId, String nickname, String sessionId, int presenceTtlSec, int userRoomTtlSec, long nowMs) {
        List<String> keys = List.of(
                KeyFactory.roomMeta(roomId),
                KeyFactory.roomMembers(roomId),
                KeyFactory.userRoom(userId),
                KeyFactory.presence(roomId, userId, sessionId),
                KeyFactory.roomHot(),
                KeyFactory.roomNicknames(roomId)
        );
        List<String> argv = List.of(
                String.valueOf(userId),
                nickname,
                String.valueOf(presenceTtlSec),
                String.valueOf(userRoomTtlSec),
                String.valueOf(nowMs)
        );

        try {
            @SuppressWarnings("unchecked")
            List<Object> out = (List<Object>) redisTemplate.execute(joinRoomLua, keys, argv.toArray());
            if (out == null || out.size() < 2) {
                return new JoinResult(JoinStatus.ERROR, 0);
            }

            // 방의 상태 코드
            int code  = toInt(out.get(0), -1);
            // 현재 방에 들어와 있는 인원 수
            int count = toInt(out.get(1), 0);

            return switch (code) {
                // 방 입장 OK
                case 0  -> new JoinResult(JoinStatus.OK, count);
                // 이미 방에 있는 회원
                case 4  -> new JoinResult(JoinStatus.ALREADY, count);
                // 방 상태 FULL
                case 3  -> new JoinResult(JoinStatus.FULL, count);
                // 방이 현재 없거나 닫혀있는 경우
                case 1  -> new JoinResult(JoinStatus.CLOSED_OR_NOT_FOUND, count);
                // 에러
                default -> new JoinResult(JoinStatus.ERROR, count);
            };

        } catch (DataAccessException e) {
            return new JoinResult(JoinStatus.ERROR, 0);
        }
    }

    private static int toInt(Object o, int fallback) {
        if (o == null) return fallback;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(Objects.toString(o));
        } catch (Exception ignore) {
            return fallback;
        }
    }
}
