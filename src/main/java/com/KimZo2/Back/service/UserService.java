package com.KimZo2.Back.service;

import com.KimZo2.Back.model.User;
import com.KimZo2.Back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public void validateDuplicateNickName(User user) {
        log.info("UserService - 닉네임 중복 검사 실행");

        if (userRepository.existsByNickname(user.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        userRepository.save(user);
    }
}
