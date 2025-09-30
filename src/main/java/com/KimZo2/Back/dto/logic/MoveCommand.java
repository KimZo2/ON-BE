package com.KimZo2.Back.dto.logic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveCommand {
    // 절대 좌표 모드
    private Double x;
    private Double y;

    // 순서/동기화
    private Long seq;

    // 방향 : "up" , "down" , "left" , "right"
    private String direction;

    private boolean isMoving;

    /**
     * 기본 형식 검증: (x,y) 또는 (dx,dy) 중 하나는 필수, NaN 금지, seq >= 0
     */
    public void validateOrThrow() {
        boolean hasAbs = (x != null && y != null);
        if (!hasAbs) {
            throw new IllegalArgumentException("Either (x,y) or (dx,dy) must be provided.");
        }
        if (x != null && (x.isNaN() || x.isInfinite())) {
            throw new IllegalArgumentException("x must be a finite number.");
        }
        if (y != null && (y.isNaN() || y.isInfinite())) {
            throw new IllegalArgumentException("y must be a finite number.");
        }
        if (seq == null || seq < 0) {
            throw new IllegalArgumentException("seq must be non-negative.");
        }
    }
}
