package com.KimZo2.Back.service;

import com.KimZo2.Back.dto.logic.LogicCode;
import com.KimZo2.Back.dto.logic.MoveAck;
import com.KimZo2.Back.dto.logic.MoveCommand;
import com.KimZo2.Back.dto.logic.Snapshot;
import com.KimZo2.Back.repository.redis.PositionRepository;
import com.KimZo2.Back.repository.redis.PresenceRepository;
import com.KimZo2.Back.repository.redis.RateLimitRepository;
import com.KimZo2.Back.repository.redis.RoomFunctionRepository;
import com.KimZo2.Back.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogicService {

    private final PresenceRepository presenceRepository;
    private final PositionRepository positionRepository;
    private final RateLimitRepository rateRepository;
    private final RoomFunctionRepository roomFunctionRepository;

    private final SimpMessagingTemplate msg;

    @Value("${app.room.session.presence-ttl-seconds:120}") private int presenceTtlSec;
    @Value("${app.room.session.userroom-ttl-seconds:360}") private int userRoomTtlSec;

    @Value("${app.room.move.window-sec:5}")  private int moveWindowSec;
    @Value("${app.room.move.max-per-window:50}") private int moveMaxPerWindow;

    @Value("${app.room.hot.room-per-tick:5000}") private long roomPerTIck;
    @Value("${app.room.hot.max-room-per-tick:200}") private long maxRoomPerTick;


    // 배치 브로드캐스트 큐
    // 방 별로 최근 변경된 유저 필드만 누적 -> 주기적으로 방 별 topic 플러시
    private final Map<UUID, Queue<String>> deltaQueues = new ConcurrentHashMap<>();

    // 유저 세션 업데이트
    // ping 올 때마다 업데이트
    public void updateSession(UUID roomId, UUID userId, String sessionId, long nowMs) {
        presenceRepository.updateSession(roomId, userId, sessionId, presenceTtlSec, userRoomTtlSec, nowMs);
    }

    // 유저 x,y 좌표 업데이트
    public MoveAck updatePosition(UUID roomId, UUID userId, String sessionId, MoveCommand cmd) {
        long now = System.currentTimeMillis();

        // 요청 빈도 제한 -> 5초 안에 50번 요청 하면 비정상 요청 처리
        long c = rateRepository.incrWithWindow(KeyFactory.moveRate(roomId, userId), moveWindowSec);
        if (c > moveMaxPerWindow) return ack(false, LogicCode.valueOf("RATE_LIMIT"), cmd.getSeq(), now);

        double x = cmd.getX();
        double y = cmd.getY();

        var res = positionRepository.userMoveLogic(
                roomId, userId, sessionId,
                x, y, now,
                Optional.ofNullable(cmd.getSeq()).orElse(0L),
                presenceTtlSec
        );

        if ("NOT_MEMBER".equals(res.status())) return ack(false, LogicCode.valueOf("NOT_MEMBER"), cmd.getSeq(), now);
        if ("STALE".equals(res.status()))     return ack(false, LogicCode.valueOf("STALE"),     res.seq(), now);

        String packed = userId + ":" + x + "," + y + "," + now;
        deltaQueues.computeIfAbsent(roomId, k -> new ConcurrentLinkedQueue<>()).offer(packed);
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

    @Scheduled(fixedDelay = 5_000)
    public void pushHotRooms() {
        long now = System.currentTimeMillis();
        long from = now - roomPerTIck;

        Set<String> hot = roomFunctionRepository.roomRecentHot(from, now);

        if (hot == null || hot.isEmpty()) return;

        int count = 0;
        for (String ridStr : hot) {
            if (count++ >= maxRoomPerTick) break; // 한 틱 과다 전송 방지
            UUID roomId = UUID.fromString(ridStr);

            var positions = positionRepository.loadAll(roomId);
            if (positions == null || positions.isEmpty()) continue;

            var s = new Snapshot();
            s.setPositions(positions);
            s.setServerTs(System.currentTimeMillis());
            msg.convertAndSend("/topic/room/" + roomId + ".pos-snapshot", s);
        }
    }

    @Scheduled(fixedDelayString = "#{1000 / (${app.room.broadcast.batch-hz:15})}")
    public void flushBatches() {
        for (var e : deltaQueues.entrySet()) {
            UUID roomId = e.getKey();
            Queue<String> q = e.getValue();
            if (q.isEmpty()) continue;

            List<String> updates = new ArrayList<>(64);
            while (!q.isEmpty() && updates.size() < 512) updates.add(q.poll());

            if (!updates.isEmpty()) {
                Map<String,Object> frame = Map.of("updates", updates, "serverTs", Instant.now().toEpochMilli());
                msg.convertAndSend("/topic/room/" + roomId + "/pos", frame);
            }
        }
    }

}
