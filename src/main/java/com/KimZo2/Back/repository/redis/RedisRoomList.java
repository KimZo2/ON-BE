package com.KimZo2.Back.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class RedisRoomList {
    private final StringRedisTemplate redis;
    private static final String Z_PUBLIC = "rooms:public";
    private static final String H_PREFIX = "roomName:";

    // 전체 public 페이지 수 찾기
    public long countPublic() {
        return Optional.ofNullable(redis.opsForZSet().zCard(Z_PUBLIC)).orElse(0L);
    }

    // 정해진 수 만큼 public 방  최신순으로 roomId 추출
    public List<String> findPublicIdsDesc(long offset, int limit) {
        if (limit <= 0) return List.of();
        long start = offset;
        long stop  = offset + limit - 1;
        Set<String> set = redis.opsForZSet().reverseRange(Z_PUBLIC, start, stop);
        return (set == null) ? List.of() : new ArrayList<>(set); // 순서 유지
    }

    // 파이프 라인 통해 HASH 일괄 로드
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> findRoomsAsHashes(List<String> ids) {
        if (ids.isEmpty()) return List.of();

        List<Map<byte[], byte[]>> raw = (List<Map<byte[], byte[]>>) (List<?>) redis.executePipelined((RedisCallback<Object>) con -> {
            for (String id : ids) {
                con.hGetAll(keyBytes(H_PREFIX + id));
            }
            return null;
        });

        List<Map<String, String>> out = new ArrayList<>(raw.size());
        for (Map<byte[], byte[]> m : raw) {
            if (m == null || m.isEmpty()) { out.add(Map.of()); continue; }
            Map<String, String> one = new HashMap<>(m.size());
            m.forEach((k, v) -> one.put(str(k), v == null ? null : str(v)));
            out.add(one);
        }
        return out;
    }

    public void removeFromPublicIndex(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        redis.opsForZSet().remove(Z_PUBLIC, ids.toArray());
    }

    private static byte[] keyBytes(String s){ return s.getBytes(StandardCharsets.UTF_8); }
    private static String str(byte[] b){ return new String(b, StandardCharsets.UTF_8); }
}
