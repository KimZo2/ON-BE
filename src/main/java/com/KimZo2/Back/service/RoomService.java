package com.KimZo2.Back.service;

import com.KimZo2.Back.dto.room.RoomCreateDTO;
import com.KimZo2.Back.dto.room.RoomListItemResponse;
import com.KimZo2.Back.dto.room.RoomPageResponse;
import com.KimZo2.Back.entity.Room;
import com.KimZo2.Back.entity.User;
import com.KimZo2.Back.repository.RoomRepository;
import com.KimZo2.Back.repository.UserRepository;
import com.KimZo2.Back.repository.redis.RedisRoomList;
import com.KimZo2.Back.repository.redis.RedisRoomStore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final RedisRoomStore redisRoomStore;
    private final RedisRoomList redisRoomList;

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
                .roomType(dto.getRoomType()) // 룸 타입 일단 defualt 1
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

    // 방 조회
    public RoomPageResponse searchRoom(int page, int size){
        // page와 size 정규화
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 10); // 오류 방지를 위해 최대 10개로만

        // 전체 public 인덱스 개수
        long total = redisRoomList.countPublic();
        // 만약 public 방이 0개라면 list 0개 return
        if (total == 0) {
            return new RoomPageResponse(1, s, 0, 0, false, List.of());
        }

        // 만약 total 페이지가 요청한 페이지 수보다 적다면
        int totalPages = (int) Math.ceil((double) total / s);
        if (p > totalPages) p = totalPages; // 마지막 페이지로

        // 생성 최신순으로 roomId 추출
        long offset = (long) (p - 1) * s;
        List<String> ids = redisRoomList.findPublicIdsDesc(offset, s);

        // 파이프라인으로 Hash 일괄 로드 -> redis와의 RTT 줄이기
        List<Map<String, String>> hashes = redisRoomList.findRoomsAsHashes(ids);

        List<RoomListItemResponse> items = new ArrayList<>(ids.size());
        List<String> prune = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            Map<String, String> h = hashes.get(i);

            if (h == null || h.isEmpty()) { prune.add(id); continue; }

            // 비즈니스 규칙
            String visibility = h.getOrDefault("visibility", "0"); // 0=PUBLIC, 1=PRIVATE
            String active = h.getOrDefault("active", "true");
            if (!"0".equals(visibility) || !"true".equalsIgnoreCase(active)) { prune.add(id); continue; }

            // DTO 매핑
            items.add(new RoomListItemResponse(
                    id,
                    h.getOrDefault("roomName", ""),
                    toInt(h.get("roomCurrentPersonCnt"), 0),
                    toInt(h.get("roomMaximumPersonCnt"), 0),
                    toInt(h.get("roomBackgroundImg"), 1)
            ));
        }

        // 정합성 보수 -> redis HASH에는 사라졋지만 zset인덱스에는 남아있음
        if (!prune.isEmpty()) redisRoomList.removeFromPublicIndex(prune);

        boolean hasNext = p < totalPages;
        return new RoomPageResponse(p, s, total, totalPages, hasNext, items);
    }

    private static int toInt(String s, int def) {
        try { return (s == null) ? def : Integer.parseInt(s); }
        catch (NumberFormatException e) { return def; }
    }

}
