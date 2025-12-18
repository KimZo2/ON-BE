package com.KimZo2.Back.domain.room.repository;

import com.KimZo2.Back.global.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RoomListRepositoryImpl implements RoomListRepository {
    private static final Logger log = LoggerFactory.getLogger(RoomListRepositoryImpl.class);

    private final StringRedisTemplate redis;

    private static final String H_PREFIX = "rooms:";

    @Override
    // 전체 public 페이지 수 찾기
    public long countPublic() {
        return Optional.ofNullable(redis.opsForZSet().zCard(KeyFactory.roomPublic())).orElse(0L);
    }

    @Override
    // 정해진 수 만큼 public 방  최신순으로 roomId 추출
    public List<String> findPublicIdsDesc(long offset, int limit) {
        if (limit <= 0) return List.of();
        long start = offset;
        long stop  = offset + limit - 1;
        Set<String> set = redis.opsForZSet().reverseRange(KeyFactory.roomPublic(), start, stop);
        return (set == null) ? List.of() : new ArrayList<>(set); // 순서 유지
    }

    @Override
    // 파이프 라인 통해 HASH 일괄 로드
    public List<Map<String, String>> findRoomsAsHashes(List<String> ids) {
        if (ids.isEmpty()) return List.of();

        SessionCallback<Object> cb = new SessionCallback<>() {
            @Override
            @SuppressWarnings("unchecked")
            public Object execute(RedisOperations operations) {
                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                for (String id : ids) {
                    ops.opsForHash().entries(H_PREFIX + id);
                }
                return null;
            }
        };

        List<Object> pipelined = redis.executePipelined(cb);

        List<Map<String, String>> out = new ArrayList<>(pipelined.size());
        for (Object o : pipelined) {
            if (o == null) { out.add(Map.of()); continue; }
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) o;
            if (m.isEmpty()) { out.add(Map.of()); continue; }
            Map<String, String> one = new HashMap<>(m.size());
            m.forEach((k, v) -> one.put((String) k, (String) v));
            out.add(one);
        }
        return out;
    }

    @Override
    public void removeFromPublicIndex(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        redis.opsForZSet().remove(KeyFactory.roomPublic(), ids.toArray());
    }
}
