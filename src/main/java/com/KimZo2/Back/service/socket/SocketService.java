package com.KimZo2.Back.service.socket;

import com.KimZo2.Back.entity.Room;
import com.KimZo2.Back.exception.ws.BadPasswordException;
import com.KimZo2.Back.exception.ws.RoomFullException;
import com.KimZo2.Back.exception.ws.RoomNotFoundOrExpiredException;
import com.KimZo2.Back.repository.RoomRepository;
import com.KimZo2.Back.repository.redis.RedisFunction;
import com.KimZo2.Back.repository.redis.RoomJoinScript;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocketService {
    private final RedisFunction redisFunction;
    private final RoomJoinScript roomJoinScript;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    // 방 입장
    public void checkRoom(UUID roomId, String principalName, String roomPW ) {
        UUID userId = UUID.fromString(principalName);
        String roomKey = "room:" + roomId;

        // 방 ID를 기준으로 방 찾기
        if (!redisFunction.roomHasKey(roomKey)) {
            throw new RoomNotFoundOrExpiredException("방이 존재하지 않거나 만료되었습니다.");
        }

        // 만약 방이 private이라면 비밀번호 요구  -> PostGre에서 검증하기
        if (redisFunction.rommIsPrivate(roomKey)) {
            checekPassword(roomPW, roomId);
        }

        // 현재 방의 인원이 가득 찼는지 확인
        long now = System.currentTimeMillis();
        long r = roomJoinScript.tryJoin(roomId, userId, now);
        if (r == -1) throw new RoomNotFoundOrExpiredException("방이 존재하지 않거나 만료되었습니다.");
        if (r == -2) throw new RoomFullException("정원이 가득 찼습니다.");

        // 인원 추가 로직
    }


    // 방 비밀번호 조회
    public void checekPassword(String roomPW, UUID roomId) {
        if (roomPW == null || roomPW.isBlank()) {
            throw new BadPasswordException("비밀번호가 필요합니다.");
        }
        // 방 ID를 기준으로 방 찾기
        Room roomEntity = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadPasswordException("방 정보를 찾을 수 없습니다."));

        // 종료된 방인지 확인
        if (!roomEntity.isStatus()) {
            throw new BadPasswordException("이미 종료된 방입니다.");
        }

        // room 비밀번호 가져오기
        String hash = roomEntity.getPassword();
        // 비밀번호 확인
        if (hash == null || !passwordEncoder.matches(roomPW, hash)) {
            throw new BadPasswordException("비밀번호가 올바르지 않습니다.");
        }
    }
}
