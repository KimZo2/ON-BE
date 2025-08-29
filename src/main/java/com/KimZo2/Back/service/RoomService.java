package com.KimZo2.Back.service;

import com.KimZo2.Back.dto.room.RoomCreateDTO;
import com.KimZo2.Back.entity.Room;
import com.KimZo2.Back.entity.User;
import com.KimZo2.Back.exception.ws.BadPasswordException;
import com.KimZo2.Back.exception.ws.RoomFullException;
import com.KimZo2.Back.exception.ws.RoomNotFoundOrExpiredException;
import com.KimZo2.Back.repository.RoomRepository;
import com.KimZo2.Back.repository.UserRepository;
import com.KimZo2.Back.repository.redis.RedisFunction;
import com.KimZo2.Back.repository.redis.RedisRoomStore;
import com.KimZo2.Back.repository.redis.RoomJoinScript;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final RedisRoomStore redisRoomStore;
    private final RedisFunction redisFunction;
    private final RoomJoinScript roomJoinScript;

    // 방 생성 -> PostGre랑 Redis에 모두 저장 필요
    public UUID createRoom(RoomCreateDTO dto) {
        String creatorNickname = dto.getCreatorNickname();

        // user 정보 찾기
        User creator = userRepository.findByNickname(creatorNickname);
        if(creator == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        // roomId 생성
        UUID roomId = UUID.randomUUID();

        // TTL 시간 설정
        Duration roomTTL = Duration.ofHours(dto.getRoomTime());

        // 활성화 되어 있는 방 중 중복 이름 판단 - Redis
        String roomName = dto.getName().trim().toLowerCase(Locale.ROOT); // 소문자/대문자/띄어쓰기 방 이름 모두 중복 판단
        if(!validateDuplicatedRoomName(roomName, roomId, roomTTL)){
            throw new IllegalStateException("이미 활성화된 같은 이름의 방이 있습니다.");
        }

        // PostGre 레코드 생성
        Room newRoom = Room.builder()
                .name(dto.getName())
                .maxParticipants(dto.getMaxParticipants())
                .isPrivate(false)
                .roomTime(dto.getRoomTime())
                .roomType(1) // 룸 타입 일단 defualt 1
                .creator(creator)
                .build();

        // room private, 비밀번호 Encode 설정
        if(dto.isPrivate()) {
            String pwdRaw = dto.getPassword();
            if(pwdRaw == null || pwdRaw.isBlank()) throw new IllegalArgumentException("비밀방 비밀번호 필요");
            newRoom.makePrivate(passwordEncoder.encode(pwdRaw));
        }

        // PostGre 커밋
        roomRepository.save(newRoom);

        // Reids 런타임 세팅
        try {
            long now = System.currentTimeMillis();
            redisRoomStore.createRoomRuntime(
                    roomId,
                    dto.getName(),
                    dto.isPrivate(),
                    creator.getId(),
                    dto.getMaxParticipants(),
                    dto.getRoomType(),
                    roomTTL,
                    now
            );
        } catch (RuntimeException e) {
            // Redis 저장 실패 : 이름 락 해제
            redisRoomStore.releaseNameLock(roomName);

            // 트랜잭션 실패 반영 로직 둘거임?
            throw e;
        }

        return roomId;
    }

    // 방 이름 중복 조회 -> 현재 방의 상태가 active인 방 중 중복 이름 존재하는지 확인하는 로직
    public boolean validateDuplicatedRoomName(String roomName, UUID roomId, Duration roomTTL) {
        return redisRoomStore.lockRoomName(roomName, roomId.toString(), roomTTL);
    }

    // 방 입장
    public void checkRoom(UUID roomId, String principalName, String roomPW ) {
        UUID userId = UUID.fromString(principalName);
        String roomKey = "room:" + roomId;

        // 방 ID를 기준으로 방 찾기
        if (!redisFunction.roomHasKey(roomKey)) {
            throw new RoomNotFoundOrExpiredException("방이 존재하지 않거나 만료되었습니다.");
        }

        // 만약 방이 private이라면 비밀번호 요구  -> PostGre에서 검증하기
        if (redisFunction.rommIsPrivate(roomKey)) {
            checekPassword(roomPW, roomId);
        }

        // 현재 방의 인원이 가득 찼는지 확인
        long now = System.currentTimeMillis();
        long r = roomJoinScript.tryJoin(roomId, userId, now);
        if (r == -1) throw new RoomNotFoundOrExpiredException("방이 존재하지 않거나 만료되었습니다.");
        if (r == -2) throw new RoomFullException("정원이 가득 찼습니다.");

    }


    // 방 비밀번호 조회
    public void checekPassword(String roomPW, UUID roomId) {
        if (roomPW == null || roomPW.isBlank()) {
            throw new BadPasswordException("비밀번호가 필요합니다.");
        }
        // 방 ID를 기준으로 방 찾기
        Room roomEntity = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadPasswordException("방 정보를 찾을 수 없습니다."));

        // 종료된 방인지 확인
        if (!roomEntity.isStatus()) {
            throw new BadPasswordException("이미 종료된 방입니다.");
        }

        // room 비밀번호 가져오기
        String hash = roomEntity.getPassword();
        // 비밀번호 확인
        if (hash == null || !passwordEncoder.matches(roomPW, hash)) {
            throw new BadPasswordException("비밀번호가 올바르지 않습니다.");
        }
    }

    // 페이지 찾기 로직
    // 몇 번째 페이지를 보여줄 것인지, 한 페이지에 몇개의 방을 보여줄것인지

    // public 처리가 되어 있는 방중
    //



}
