package com.KimZo2.Back.domain.roomlogic.repository;

import com.KimZo2.Back.domain.room.dto.RoomPosResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PositionRepository {
    /** moveLua 실행 결과: status, appliedSeq, version */
    MoveResult userMoveLogic(UUID roomId, UUID userId, String sessionId,
                          String nickname, double x, double y, long ts, long seq,
                          int presenceTtlSec, String direction, boolean isMoving);

    List<RoomPosResponseDTO> loadAll(UUID roomId);

//    Long currentVersion(UUID roomId);

    record MoveResult(String status, long seq) {}
}
