package com.KimZo2.Back.repository.redis;

import java.util.List;
import java.util.UUID;

public interface PositionRepository {
    /** moveLua 실행 결과: status, appliedSeq, version */
    MoveResult userMoveLogic(UUID roomId, UUID userId, String sessionId,
                          double x, double y, long ts, long seq,
                          int presenceTtlSec);

    List<String> loadAll(UUID roomId);

//    Long currentVersion(UUID roomId);

    record MoveResult(String status, long seq, long version) {}
}
