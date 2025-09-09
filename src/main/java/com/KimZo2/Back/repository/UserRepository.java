package com.KimZo2.Back.repository;

import com.KimZo2.Back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // user nickname 중복 방지
    boolean existsByNickname(String nickname);

    // user 정보 반환
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // user nickname으로 찾기
    User findByNickname(String nickname);

}
