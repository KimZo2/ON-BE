package com.KimZo2.Back.service;

import com.KimZo2.Back.dto.logic.UserLeaveDTO;
import com.KimZo2.Back.dto.room.JoinResult;
import com.KimZo2.Back.dto.room.RoomEnterResponseDTO;
import com.KimZo2.Back.exception.ws.BadPasswordException;
import com.KimZo2.Back.exception.ws.RoomNotFoundOrExpiredException;
import com.KimZo2.Back.model.Room;
import com.KimZo2.Back.repository.RoomRepository;
import com.KimZo2.Back.repository.redis.JoinRepository;
import com.KimZo2.Back.repository.redis.RoomCleanUpRepository;
import com.KimZo2.Back.repository.redis.RoomFunctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocketService {
    private final PasswordEncoder passwordEncoder;

    private final RoomRepository roomRepository;

    private final JoinRepository joinRepository;
    private final RoomCleanUpRepository roomCleanUpRepository;
    private final RoomFunctionRepository roomFunctionRepository;


    private final SimpMessagingTemplate msg;

    // 세션 단위 생존 시간 TTL
    @Value("${app.room.session.presence-ttl-seconds:120}") private int presenceTtlSec;
    // 유저가 어느 방에 속해 있는 역참조
    @Value("${app.room.session.userroom-ttl-seconds:360}") private int userRoomTtlSec;

    // 방 입장
    public void checkRoom(UUID roomId, String roomPW ) {

        // 방 ID를 기준으로 방 찾기
        if (!roomFunctionRepository.roomExists(roomId)) {
            throw new RoomNotFoundOrExpiredException("방이 존재하지 않거나 만료되었습니다.");
        }

        // 만약 방이 private이라면 비밀번호 요구  -> PostGre에서 검증하기
        if (roomFunctionRepository.roomIsPrivate(roomId)) {
            checkPassword(roomPW, roomId);
        }
    }

    // 방 비밀번호 조회
    private void checkPassword(String roomPW, UUID roomId) {
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

    // 방 입장 로직
    public void joinRoom(UUID roomId, UUID userId, String sessionId) {
        long nowMs = System.currentTimeMillis();
        // 방 입장
        JoinResult result = joinRepository.join(roomId, userId, sessionId, presenceTtlSec, userRoomTtlSec, nowMs);

        switch (result.status()) {
            case OK -> {
                // 브로드캐스트
                msg.convertAndSend("/topic/room." + roomId + "/msg",
                        new RoomEnterResponseDTO(roomId, "JOIN", result.count()));
                // 개인 응답
                msg.convertAndSendToUser(userId.toString(), "/queue/join",
                        new RoomEnterResponseDTO(roomId, "JOIN", result.count()));
            }
            case ALREADY, FULL, CLOSED_OR_NOT_FOUND, ERROR ->
                    msg.convertAndSendToUser(userId.toString(), "/queue/join",
                            new RoomEnterResponseDTO(roomId, result.status().name(), result.count()));
        }
    }

    // 방 퇴장 로직
    public void leaveRoom(UUID roomId, UUID userId){
        Long result =  roomCleanUpRepository.cleanupExpiredUser(roomId, userId.toString());

        if (result != null && result == 1) {
            log.info("User {} explicitly left room {}.", userId, roomId);

            // 3. 같은 방의 다른 유저들에게 퇴장 알림 메시지 전송
            UserLeaveDTO payload = new UserLeaveDTO(userId.toString(), "SELF_LOGOUT");
            String destination = "/topic/room/" + roomId + "/leave";

            msg.convertAndSend(destination, payload);
        } else {
            log.warn("User {} leave request for room {} failed or user was already gone.", userId, roomId);
        }
    }
}
