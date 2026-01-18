package com.KimZo2.Back.domain.roomschedule;

import com.KimZo2.Back.domain.roomlogic.dto.UserLeaveDTO;
import com.KimZo2.Back.domain.roomlogic.dto.Snapshot;
import com.KimZo2.Back.domain.roomlogic.repository.PositionRepository;
import com.KimZo2.Back.domain.roomlogic.repository.PresenceRepository;
import com.KimZo2.Back.domain.roomparticipation.repository.RoomCleanUpRepositoryImpl;
import com.KimZo2.Back.domain.roomparticipation.repository.RoomFunctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final RoomFunctionRepository roomFunctionRepository;
    private final PositionRepository positionRepository;
    private final PresenceRepository presenceRepository;
    private final RoomCleanUpRepositoryImpl roomCleanUpRepository;
    private final SimpMessagingTemplate msg;

    @Value("${app.room.hot.room-per-tick:5000}")
    private long roomPerTIck;
    @Value("${app.room.hot.max-room-per-tick:200}")
    private long maxRoomPerTick;
    @Value("${app.room.session.presence-ttl-seconds:120}")
    private int presenceTtlSec;

    // 유저가 최근에 움직인 방의 모든 좌표를 검증하는 로직
    @Scheduled(fixedDelay = 5_000)
    public void pushHotRooms() {
        log.debug("Starting pushHotRooms task.");
        long now = System.currentTimeMillis();
        long from = now - roomPerTIck;

        Set<String> hot = roomFunctionRepository.roomRecentHot(from, now);

        if (hot == null || hot.isEmpty()) {
            log.debug("No hot rooms found to push.");
            return;
        }

        log.info("Found {} hot room(s) to process.", hot.size());
        int count = 0;
        for (String ridStr : hot) {
            if (count >= maxRoomPerTick) {
                log.debug("Reached max rooms per tick ({}). Breaking loop.", maxRoomPerTick);
                break; // 한 틱 과다 전송 방지
            }

            try {
                UUID roomId = UUID.fromString(ridStr);
                var positions = positionRepository.loadAll(roomId);
                if (positions == null || positions.isEmpty()) continue;

                var s = new Snapshot();
                s.setPositions(positions);
                s.setServerTs(System.currentTimeMillis());
                msg.convertAndSend("/topic/room/" + roomId + "/pos-snapshot", s);
                count++;
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format for hot room ID: {}", ridStr);
            } catch (Exception e) {
                log.error("Error processing hot room ID {}: {}", ridStr, e.getMessage());
            }
        }
        log.info("Finished pushHotRooms task. Processed {} room(s).", count);
    }

    // 유저 세션 관리
    @Scheduled(fixedDelay = 120000)
    public void cleanupExpiredUsers() {
        Set<String> allRoomIds = roomFunctionRepository.allRoomIds();
        if (allRoomIds == null || allRoomIds.isEmpty()) {
            return;
        }

        for (String roomId : allRoomIds) {
            Set<String> expiredUserIds = presenceRepository.findExpiredUserInRoom(roomId, presenceTtlSec);
            if (expiredUserIds.isEmpty()) {
                continue;
            }
            for (String userId : expiredUserIds) {
                try {
                    Long result = roomCleanUpRepository.cleanupExpiredUser(UUID.fromString(roomId), userId);

                    // Repository의 결과에 따라 STOMP 메시지 전송
                    if (result != null && result == 1) {
                        UserLeaveDTO payload = new UserLeaveDTO(userId, "SESSION_EXPIRED");
                        String destination = "/topic/room/" + roomId + "/leave";

                        msg.convertAndSend(destination, payload);
                    }
                } catch (Exception e) {
                    log.error("Error during cleanup for user {} in room {}: {}", userId, roomId, e.getMessage());
                }
            }
        }
        log.info("Finished expired user cleanup task.");
    }

    // 방 더미 데이터 정리
    @Scheduled(fixedDelay = 600000)
    public void cleanupRoomDummyData() {
        log.info("Starting orphaned room data cleanup task.");
        Set<String> allRoomIds = roomFunctionRepository.allRoomIds();
        if (allRoomIds == null || allRoomIds.isEmpty()) {
            log.info("No rooms found to clean up.");
            return;
        }

        int cleanupCount = 0;
        for (String roomIdStr : allRoomIds) {
            try {
                UUID roomId = UUID.fromString(roomIdStr);
                // Check if the main room meta key exists
                if (!roomFunctionRepository.roomExists(roomId)) {
                    log.info("Found orphaned room data for roomId: {}. Cleaning up.", roomId);
                    roomCleanUpRepository.deleteAllRoomData(roomId);
                    cleanupCount++;
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format found in room list: {}", roomIdStr);
            } catch (Exception e) {
                log.error("Error during cleanup for room ID {}: {}", roomIdStr, e.getMessage());
            }
        }
        log.info("Finished orphaned room data cleanup task. Cleaned up {} orphaned room(s).", cleanupCount);
    }

}
