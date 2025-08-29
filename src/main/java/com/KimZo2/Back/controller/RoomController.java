package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.room.RoomCreateDTO;
import com.KimZo2.Back.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;

    // 방 생성
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody RoomCreateDTO dto) {
        log.info("RoomController - GET /rooms  -  실행");

        roomService.createRoom(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Room 생성 완료"));
    }


    // public 방 조회
//    @GetMapping("/room")
//    public ResponseEntity<?>




    // private 방 조회




    // 방 입장



}
