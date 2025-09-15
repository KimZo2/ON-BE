package com.KimZo2.Back.repository.redis;

import com.KimZo2.Back.dto.room.RoomPosResponseDTO;
import com.KimZo2.Back.util.KeyFactory;
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
    public MoveResult userMoveLogic(UUID roomId, UUID userId, String sessionId, double x, double y, long ts, long seq, int presenceTtlSec, String direction, boolean isMoving) {
        List<String> keys = List.of(
                KeyFactory.roomMembers(roomId),
                KeyFactory.presence(roomId, userId, sessionId),
                KeyFactory.roomPos(roomId),
                KeyFactory.roomSeen(roomId),
                KeyFactory.roomHot()
        );
        List<String> argv = List.of(
                String.valueOf(userId), sessionId,
                String.valueOf(x), String.valueOf(y),
                String.valueOf(ts),
                String.valueOf(seq),
                String.valueOf(presenceTtlSec),
                roomId.toString(),
                direction,
                Boolean.toString(isMoving)
        );

        @SuppressWarnings("unchecked")
        List<Object> res = (List<Object>)(List<?>) redisTemplate.execute(userMoveLua, keys, argv.toArray());
        String status = String.valueOf(res.get(0));
        long appliedSeq = Long.parseLong(String.valueOf(res.get(1)));
        long ver = Long.parseLong(String.valueOf(res.get(2)));
        return new MoveResult(status, appliedSeq, ver);
    }


    @Override
    public List<RoomPosResponseDTO> loadAll(UUID roomId) {
        Map<Object,Object> m = redisTemplate.opsForHash().entries(KeyFactory.roomPos(roomId));
        List<RoomPosResponseDTO> out = new ArrayList<>(m.size());

        m.forEach((k, v) -> {
            UUID userId = UUID.fromString(String.valueOf(k));
            String[] parts = String.valueOf(v).split(",", 6);

            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            long seq = Long.parseLong(parts[3]);
            String direction = parts[4];
            boolean isMoving = Boolean.parseBoolean(parts[5]);

            out.add(new RoomPosResponseDTO(userId, x, y, seq, direction, isMoving));
        });

        return out;
    }


}
