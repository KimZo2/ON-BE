package com.KimZo2.Back.dto.logic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserLeaveDTO {
    private final String userId;
    private final String reason;
}
