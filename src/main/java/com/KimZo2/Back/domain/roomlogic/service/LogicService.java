package com.KimZo2.Back.domain.roomlogic.service;

import com.KimZo2.Back.domain.roomlogic.dto.LogicCode;
import com.KimZo2.Back.domain.roomlogic.dto.MoveAck;
import com.KimZo2.Back.domain.roomlogic.dto.MoveCommand;
import com.KimZo2.Back.domain.roomlogic.dto.Snapshot;
import com.KimZo2.Back.domain.room.dto.RoomPosResponseDTO;
import com.KimZo2.Back.domain.roomlogic.repository.PositionRepository;
import com.KimZo2.Back.domain.roomlogic.repository.PresenceRepository;
import com.KimZo2.Back.domain.roomlogic.repository.RateLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogicService {

    private final PresenceRepository presenceRepository;
    private final PositionRepository positionRepository;
    private final RateLimitRepository rateRepository;
    private final SimpMessagingTemplate msg;

    @Value("${app.room.session.presence-ttl-seconds:120}") private int presenceTtlSec;
    @Value("${app.room.session.userroom-ttl-seconds:360}") private int userRoomTtlSec;

    @Value("${app.room.move.window-sec:5}")  private int moveWindowSec;
    @Value("${app.room.move.max-per-window:50}") private int moveMaxPerWindow;

    // 배치 브로드캐스트 큐
    // Map<방 ID, Map<유저 ID, 최종 좌표 객체>>
    private final Map<UUID, Map<UUID, RoomPosResponseDTO>> deltaMaps = new ConcurrentHashMap<>();

    // 유저 세션 업데이트
    // ping 올 때마다 업데이트
    public void updateSession(UUID roomId, UUID userId, String sessionId, long nowMs) {
        presenceRepository.updateSession(roomId, userId, sessionId, presenceTtlSec, userRoomTtlSec, nowMs);
    }

    // 유저 x,y 좌표 업데이트
    public MoveAck updatePosition(UUID roomId, UUID userId, String sessionId, MoveCommand cmd) {
        long now = System.currentTimeMillis();

        // 요청 빈도 제한 -> 5초 안에 50번 요청 하면 비정상 요청 처리
        // Client인 Parser 부분의 반복적인 요청으로 인해 이는 잠시 미룸
//        long c = rateRepository.incrWithWindow(KeyFactory.moveRate(roomId, userId), moveWindowSec);
//        if (c > moveMaxPerWindow) return ack(false, LogicCode.RATE_LIMIT, cmd.getSeq(), now);

        String nickname = cmd.getNickname();
        double x = cmd.getX();
        double y = cmd.getY();
        long seq = cmd.getSeq();
        String direction = cmd.getDirection();
        boolean isMoving = cmd.getIsMoving();
        int avatar = cmd.getAvatar();

        var res = positionRepository.userMoveLogic(
                roomId, userId, sessionId,
                nickname, x, y, now,
                Optional.ofNullable(cmd.getSeq()).orElse(0L),
                presenceTtlSec, direction, avatar, isMoving
        );

        if ("NOT_MEMBER".equals(res.status())) return ack(false, LogicCode.valueOf("NOT_MEMBER"), cmd.getSeq(), now);

        // 맵을 가져오거나 새로 생성
        Map<UUID, RoomPosResponseDTO> roomUpdates = deltaMaps.computeIfAbsent(
                roomId,
                k -> new ConcurrentHashMap<>()
        );

        // 유저 ID를 키로 최종 좌표를 덮어씌우기
        roomUpdates.put(
                userId,
                new RoomPosResponseDTO(userId, nickname, x, y, seq, direction, avatar, isMoving)
        );

        return ack(true, LogicCode.valueOf("OK"), cmd.getSeq(), now);
    }

    // 유저 방 입장 후 -> 기존 방 인원 좌표 스냅샷
    public Snapshot loadSnapshot(UUID roomId, UUID userId) {
        var positions = positionRepository.loadAll(roomId);
        Snapshot s = new Snapshot();
        s.setPositions(positions);
        s.setServerTs(System.currentTimeMillis());
        return s;
    }

    private MoveAck ack(boolean ok, LogicCode code, Long seq, long serverTs) {
        var m = new MoveAck();
        m.setOk(ok);
        m.setCode(code);
        m.setAppliedSeq(seq);
        m.setServerTs(serverTs);
        return m;
    }

    // 큐에 저장되어 있는 유저의 움직임을 60Hz로 보내는 로직
    @Scheduled(fixedDelayString = "#{1000 / (${app.room.broadcast.batch-hz:60})}")
    public void flushBatches() {
        for (var e : deltaMaps.entrySet()) {
            UUID roomId = e.getKey();

            Map<UUID, RoomPosResponseDTO> updatesMap = e.getValue();

            // 맵에서 모든 업데이트를 가져온 후 맵을 클리어
            Map<UUID, RoomPosResponseDTO> updatesToSend = new HashMap<>();

            /**
             * Lock을 사용하기에 안전하지만 lock을 잡는 동안 updatePosition의 스레드들이 멈춰서
             * 처리율 측면에서는 약간의 성능 저하가 발생할 수도 있다.
             */
            synchronized (updatesMap) { // updateMap Lock
                if (updatesMap.isEmpty()) continue;
                updatesToSend.putAll(updatesMap);
                updatesMap.clear();
            }

            List<RoomPosResponseDTO> updates = new ArrayList<>(updatesToSend.values());

            if (!updates.isEmpty()) {
                Map<String,Object> frame = Map.of("updates", updates, "serverTs", Instant.now().toEpochMilli());
                msg.convertAndSend("/topic/room/" + roomId + "/pos", frame);
            }
        }
    }
}
