package com.KimZo2.Back.dto.logic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snapshot {
    /**
     * userId -> "x,y,ts,seq" (경량 문자열)
     * 필요 시 richer object로 교체 가능 (ex. Map<String, PositionDto>)
     */
    private List<String> positions;
    private Long serverTs;  // 서버 기준 시각(ms)
}
