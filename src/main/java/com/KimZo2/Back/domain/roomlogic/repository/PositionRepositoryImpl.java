package com.KimZo2.Back.domain.roomlogic.repository;

import com.KimZo2.Back.domain.room.dto.RoomPosResponseDTO;
import com.KimZo2.Back.global.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PositionRepositoryImpl implements PositionRepository{
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> userMoveLua;

    @Override
    public MoveResult userMoveLogic(UUID roomId, UUID userId, String sessionId, String nickname, double x, double y, long ts, long seq, int presenceTtlSec, String direction, int avatar, boolean isMoving) {
        List<String> keys = List.of(
                KeyFactory.roomMembers(roomId),
                KeyFactory.presence(roomId, userId, sessionId),
                KeyFactory.roomPos(roomId),
                KeyFactory.roomSeen(roomId),
                KeyFactory.roomHot()
        );
        List<String> argv = List.of(
                String.valueOf(userId),
                sessionId,
                String.valueOf(x),
                String.valueOf(y),
                String.valueOf(ts),
                String.valueOf(seq),
                String.valueOf(presenceTtlSec),
                roomId.toString(),
                direction,
                Boolean.toString(isMoving),
                nickname,
                String.valueOf(avatar)
        );

        @SuppressWarnings("unchecked")
        List<Object> res = (List<Object>)(List<?>) redisTemplate.execute(userMoveLua, keys, argv.toArray());
        String status = String.valueOf(res.get(0));
        long appliedSeq = Long.parseLong(String.valueOf(res.get(1)));
        return new MoveResult(status, appliedSeq);
    }


    @Override
    public List<RoomPosResponseDTO> loadAll(UUID roomId) {
        Map<Object,Object> m = redisTemplate.opsForHash().entries(KeyFactory.roomPos(roomId));
        List<RoomPosResponseDTO> out = new ArrayList<>(m.size());

        m.forEach((k, v) -> {
            UUID userId = UUID.fromString(String.valueOf(k));
            String[] parts = String.valueOf(v).split(",", 8);

            String nickname = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            long seq = Long.parseLong(parts[4]);
            String direction = parts[5];
            int avatar = Integer.parseInt(parts[6]);
            boolean isMoving = Boolean.parseBoolean(parts[7]);

            out.add(new RoomPosResponseDTO(userId, nickname, x, y, seq, direction, avatar, isMoving));
        });

        return out;
    }


}
