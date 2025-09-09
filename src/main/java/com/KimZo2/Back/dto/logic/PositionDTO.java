package com.KimZo2.Back.dto.logic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionDTO {
    private double x;
    private double y;
    private long ts;
    private long seq;

    public static PositionDTO parseCompressed(String compressed) {
        // "x,y,ts,seq"
        String[] p = compressed.split(",", 4);
        if (p.length < 4) throw new IllegalArgumentException("Invalid position string: " + compressed);
        return PositionDTO.builder()
                .x(Double.parseDouble(p[0]))
                .y(Double.parseDouble(p[1]))
                .ts(Long.parseLong(p[2]))
                .seq(Long.parseLong(p[3]))
                .build();
    }

    public String toCompressed() {
        return x + "," + y + "," + ts + "," + seq;
    }
}
