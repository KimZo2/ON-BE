package com.KimZo2.Back.domain.member.repository;

import com.KimZo2.Back.global.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    // member nickname 중복 방지
    boolean existsByNickname(String nickname);

    // member 정보 반환
    Optional<Member> findByProviderAndProviderId(String provider, String providerId);

    // member nickname으로 찾기
    Member findByNickname(String nickname);

}
