package com.KimZo2.Back.repository.redis;

import com.KimZo2.Back.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembersRepositoryImpl implements MembersRepository{
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isMember(UUID roomId, String userId) {
        Boolean b = redisTemplate.opsForSet().isMember(KeyFactory.roomMembers(roomId), userId);
        return Boolean.TRUE.equals(b);
    }

    @Override
    public void addMember(UUID roomId, String userId) {
        redisTemplate.opsForSet().add(KeyFactory.roomMembers(roomId), userId);
    }

    @Override
    public void removeMember(UUID roomId, String userId) {
        redisTemplate.opsForSet().remove(KeyFactory.roomMembers(roomId), userId);

    }

    @Override public long count(UUID roomId) {
        Long c = redisTemplate.opsForSet().size(KeyFactory.roomMembers(roomId));
        return c == null ? 0L : c;
    }
}
