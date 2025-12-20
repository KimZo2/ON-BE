package com.KimZo2.Back.domain.roomlogic.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserLeaveDTO {
    private final String userId;
    private final String reason;
}
