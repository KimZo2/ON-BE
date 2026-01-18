package com.KimZo2.Back.domain.room.service;

import com.KimZo2.Back.domain.member.repository.MemberRepository;
import com.KimZo2.Back.domain.room.dto.RoomCreateDTO;
import com.KimZo2.Back.domain.room.dto.RoomListItemResponse;
import com.KimZo2.Back.domain.room.dto.RoomPageResponse;
import com.KimZo2.Back.global.entity.Member;
import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.global.exception.ErrorCode;
import com.KimZo2.Back.global.entity.Room;
import com.KimZo2.Back.domain.room.repository.RoomRepository;
import com.KimZo2.Back.domain.room.repository.RoomListRepository;
import com.KimZo2.Back.domain.room.repository.RoomStoreRepository;
import com.KimZo2.Back.global.util.KeyFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final RoomStoreRepository roomStoreRepository;
    private final RoomListRepository roomListRepository;

    // 방 생성 -> PostGre랑 Redis에 모두 저장 필요
    @Transactional
    public String createRoom(RoomCreateDTO dto) {
        String creatorNickname = dto.getCreatorNickname();

        // member 정보 찾기
        Member creator = memberRepository.findByNickname(creatorNickname);
        if(creator == null) throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);

        // roomId 생성
        UUID roomId = UUID.randomUUID();

        // TTL 시간 설정
        Duration roomTTL = Duration.ofHours(dto.getRoomTime());

        // 활성화 되어 있는 방 중 중복 이름 판단 - Redis
        String roomName = dto.getName().trim().toLowerCase(Locale.ROOT); // 소문자/대문자/띄어쓰기 방 이름 모두 중복 판단
        if(!validateDuplicatedRoomName(roomName, roomId, roomTTL)){
            throw new CustomException(ErrorCode.DUPLICATE_ROOM_NAME);
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
            if(pwdRaw == null || pwdRaw.isBlank()) throw new CustomException(ErrorCode.PASSWORD_REQUIRED_FOR_PRIVATE_ROOM);
            newRoom.makePrivate(passwordEncoder.encode(pwdRaw));
        }

        // PostGre 커밋 (영속성 컨텍스트에만 들어가 있는 상태)
        roomRepository.save(newRoom);

        // 트랜잭션 동기화
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            // 해당 블록은 DB 트랜잭션이 성공적으로 'Commit'이 된 직후 실행
            @Override
            public void afterCommit() {
                try {
                    long now = System.currentTimeMillis();
                    roomStoreRepository.createRoomRuntime(
                            roomId,
                            dto.getName(),
                            dto.isPrivate(),
                            creator.getId(),
                            dto.getMaxParticipants(),
                            dto.getRoomType(),
                            roomTTL,
                            now
                    );
                    log.info("Redis runtime room created successfully for room: {}", roomId);
                } catch (Exception e) {
                    log.error("CRITICAL: DB committed but Redis failed to create runtime room. RoomID: {}", roomId, e);

                    // redis에 존재하는 더미데이터 삭제
                    roomStoreRepository.releaseNameLock(roomName);
                    roomStoreRepository.deleteRoomRuntimeIfPresent(roomId);

                    // 보상 트랜잭션 구축
                    try {
                        // RDB roomId 데이터 삭제
                        roomRepository.deleteById(roomId);
                        log.info("Manual rollback for DB successful. RoomID: {}", roomId);
                    } catch (Exception dbEx) {
                        log.error("FATAL: Failed to rollback DB after Redis failure. Manual intervention required. RoomID: {}", roomId, dbEx);
                    }

                    throw new CustomException(ErrorCode.ROOM_CREATION_FAILED);
                }
            }
        });

        return "방 생성 완료";
    }

    // 방 이름 중복 조회 -> 현재 방의 상태가 active인 방 중 중복 이름 존재하는지 확인하는 로직
    public boolean validateDuplicatedRoomName(String roomName, UUID roomId, Duration roomTTL) {
        return roomStoreRepository.lockRoomName(roomName, roomId.toString(), roomTTL);
    }

    // 방 조회
    public RoomPageResponse searchRoom(int page, int size){

        // page와 size 정규화
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 6); // 오류 방지를 위해 최대 6개로만

        // 전체 public 인덱스 개수
        long total = roomListRepository.countPublic();
        // 만약 public 방이 0개라면 list 0개 return
        if (total == 0) {
            return new RoomPageResponse(1, s, 0, false, List.of());
        }

        // 만약 total 페이지가 요청한 페이지 수보다 적다면
        int totalPages = (int) Math.ceil((double) total / s);
        if (p > totalPages) p = totalPages; // 마지막 페이지로

        // 생성 최신순으로 roomId 추출
        long offset = (long) (p - 1) * s;
        // Pagination Hole의 문제 해결을 위해 1.5배의 리스트 가져오기
        List<String> ids = roomListRepository.findPublicIdsDesc(offset, (int)(s * 1.5));

        // 파이프라인으로 Hash 일괄 로드 -> redis와의 RTT 줄이기
        List<Map<String, String>> hashes = roomListRepository.findRoomsAsHashes(ids);

        List<RoomListItemResponse> items = new ArrayList<>(ids.size());
        List<String> prune = new ArrayList<>();

        // int totalElement = ids.size();

        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            Map<String, String> h = hashes.get(i);

            if (h == null || h.isEmpty()) {
                prune.add(id);
                log.warn("Found ghost room (ZSet exists but Hash missing) - ID: {}", id);
                continue;
            }

            // 비즈니스 규칙
            String visibility = h.getOrDefault(KeyFactory.FIELD_VISIBILITY, KeyFactory.VALUE_PUBLIC); // 0=PUBLIC, 1=PRIVATE
            String active = h.getOrDefault(KeyFactory.FIELD_ACTIVE, KeyFactory.FIELD_TRUE);
            if (!KeyFactory.VALUE_PUBLIC.equals(visibility) || !KeyFactory.FIELD_TRUE.equalsIgnoreCase(active)) {
                prune.add(id);
                log.debug("Filtering room - ID: {}, Visibility: {}, Active: {}", id, visibility, active);
                continue; }

            // DTO 매핑
            items.add(new RoomListItemResponse(
                    id,
                    h.getOrDefault("roomName", ""),
                    toInt(h.get("roomCurrentPersonCnt"), 0),
                    toInt(h.get("roomMaximumPersonCnt"), 0),
                    toInt(h.get("roomBackgroundImg"), 1)
            ));
        }

        if (items.size() > s) {
            items = items.subList(0, s);
        }

        // 정합성 보수 -> redis HASH에는 사라졋지만 zset인덱스에는 남아있음
        if (!prune.isEmpty()){
            log.info("Self-healing triggered: Removing {} invalid IDs from ZSet. IDs: {}", prune.size(), prune);
            roomListRepository.removeFromPublicIndex(prune);
        }

        boolean hasNext = p < totalPages;
        return new RoomPageResponse(p, s, items.size(), hasNext, items);
    }

    private static int toInt(String s, int def) {
        try { return (s == null) ? def : Integer.parseInt(s); }
        catch (NumberFormatException e) { return def; }
    }
}
