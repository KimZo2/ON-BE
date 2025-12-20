package com.KimZo2.Back.domain.roomlogic.dto;

import com.KimZo2.Back.domain.room.dto.RoomPosResponseDTO;
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
    private List<RoomPosResponseDTO> positions;
    private Long serverTs;  // 서버 기준 시각(ms)
}
