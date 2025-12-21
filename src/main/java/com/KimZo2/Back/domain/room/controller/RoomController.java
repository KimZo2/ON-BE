package com.KimZo2.Back.domain.room.controller;

import com.KimZo2.Back.domain.room.dto.RoomCreateDTO;
import com.KimZo2.Back.domain.room.dto.RoomPageResponse;
import com.KimZo2.Back.domain.room.service.RoomService;
import com.KimZo2.Back.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import com.KimZo2.Back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name="Room", description = "rooms 생성, 조회 API")
@RestController
@Validated
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;

    // 방 생성
    @Operation(
            summary = "방 생성",
            description = "새로운 방을 생성합니다.<br>" +
                    "**비공개(private) 방**을 생성할 경우 **비밀번호**는 필수입니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "방 생성 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (비공개 방 비밀번호 누락 등 - ROOM_002)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 사용자 (AUTH_005)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 방 이름 (ROOM_001)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "방 생성 실패 (Redis/DB 저장 오류 - ROOM_003)",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @PostMapping
    public ApiResponse<?> createRoom(@RequestBody RoomCreateDTO dto) {

        String message = roomService.createRoom(dto);

        return ApiResponse.onSuccess(message);
    }

    // public 방 조회
    @GetMapping
    @Operation(summary = "방 List 조회", description = "페이지네이션을 통해 공개(Public) 방 목록을 조회합니다.<br>size는 **최대 6**까지 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomPageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (size 6 초과 등 - COMMON_002)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (ROOM_004)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ApiResponse<RoomPageResponse> listPublicRooms(
            @Parameter(description = "페이지 번호 (1 이상)", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") int page,

            @Parameter(description = "페이지 크기 (1 이상 6 이하)", example = "6")
            @RequestParam(defaultValue = "6")
            @Min(value = 1, message = "사이즈는 1 이상이어야 합니다.")
            @Max(value = 6, message = "사이즈는 최대 6까지 가능합니다.") int size
    ) {
        RoomPageResponse response = roomService.searchRoom(page, size);
        return ApiResponse.onSuccess(response);
    }
}
