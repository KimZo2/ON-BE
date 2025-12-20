package com.KimZo2.Back.domain.roomlogic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveAck {
    private boolean ok;
    private LogicCode code;       // OK / NOT_MEMBER / RATE_LIMIT / STALE ...
    private Long appliedSeq; // 서버가 최종 반영한 seq
    private Long serverTs;

    public static MoveAck ok(Long seq, Long serverTs, Long version) {
        return MoveAck.builder().ok(true).code(LogicCode.OK).appliedSeq(seq).serverTs(serverTs).build();
    }
    public static MoveAck fail(LogicCode code, Long seq, Long serverTs) {
        return MoveAck.builder().ok(false).code(code).appliedSeq(seq).serverTs(serverTs).build();
    }
}
