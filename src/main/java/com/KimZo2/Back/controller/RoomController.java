package com.KimZo2.Back.controller;

import com.KimZo2.Back.dto.room.RoomCreateDTO;
import com.KimZo2.Back.dto.room.RoomPageResponse;
import com.KimZo2.Back.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name="Room", description = "rooms 생성, 조회 API")
@RestController
@Validated
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;

    // 방 생성
    @PostMapping
    @Operation(summary="방 생성", description="RoomCreateDTO로 방을 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "방 생성 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 없는 Private Room 생성 오류"),
            @ApiResponse(responseCode = "409", description = "중복된 방 이름"),
            @ApiResponse(responseCode = "500", description = "Redis 저장 실패 또는 서버 오류")
    })
    public ResponseEntity<?> createRoom(@RequestBody RoomCreateDTO dto) {
        log.info("RoomController - POST /rooms  -  실행");

        roomService.createRoom(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Room 생성 완료"));
    }

    // public 방 조회
    @GetMapping
    @Operation(summary="방 List 조회", description="page, size를 통해 방 리스트 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 page/size 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public RoomPageResponse listPublicRooms(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "6") @Min(1) int size
    ) {
        log.info("RoomController - Get /rooms  -  실행");

        return roomService.searchRoom(page, size);
    }
}
