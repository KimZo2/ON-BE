package com.KimZo2.Back.domain.user.service;

import com.KimZo2.Back.domain.auth.repository.UserInfoResponseDTO;
import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.global.entity.User;
import com.KimZo2.Back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.KimZo2.Back.global.exception.ErrorCode.DUPLICATE_NICKNAME;
import static com.KimZo2.Back.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public void validateDuplicateNickName(User user) {

        if (userRepository.existsByNickname(user.getNickname())) {
            throw new CustomException(DUPLICATE_NICKNAME);
        }

        userRepository.save(user);
    }

    public UserInfoResponseDTO getUserId(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return new UserInfoResponseDTO(
                user.getId(),
                user.getNickname()
        );
    }
}
