package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.room.RoomCreateDTO;
import com.KimZo2.Back.dto.room.RoomPageResponse;
import com.KimZo2.Back.service.RoomService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;

    // 방 생성
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomCreateDTO dto) {
        log.info("RoomController - POST /rooms  -  실행");

        roomService.createRoom(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Room 생성 완료"));
    }

    // public 방 조회
    @GetMapping
    public RoomPageResponse listPublicRooms(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "6") @Min(1) int size
    ) {
        log.info("RoomController - POST /rooms  -  실행");

        return roomService.searchRoom(page, size);
    }
}
