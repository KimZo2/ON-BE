package com.KimZo2.Back.service;

import com.KimZo2.Back.dto.member.UserInfoResponseDTO;
import com.KimZo2.Back.exception.user.DuplicateUserNicknameException;
import com.KimZo2.Back.exception.user.UserNotFoundException;
import com.KimZo2.Back.model.User;
import com.KimZo2.Back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public void validateDuplicateNickName(User user) {
        log.info("UserService - 닉네임 중복 검사 실행");

        if (userRepository.existsByNickname(user.getNickname())) {
            throw new DuplicateUserNicknameException("Already exist nickname.");
        }

        userRepository.save(user);
    }

    public UserInfoResponseDTO getUserId(UUID userId) {
        log.info("UserService - userId 검색");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(("User not found.")));

        return new UserInfoResponseDTO(
                user.getId(),
                user.getNickname()
        );
    }
}
