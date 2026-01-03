package com.KimZo2.Back.domain.member.service;

import com.KimZo2.Back.domain.auth.dto.MemberInfoResponseDTO;
import com.KimZo2.Back.domain.member.repository.MemberRepository;
import com.KimZo2.Back.global.entity.Member;
import com.KimZo2.Back.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.KimZo2.Back.global.exception.ErrorCode.DUPLICATE_NICKNAME;
import static com.KimZo2.Back.global.exception.ErrorCode.MEMBER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;

    public void validateDuplicateNickName(String nickname) {

        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(DUPLICATE_NICKNAME);
        }
    }

    public MemberInfoResponseDTO getUserId(UUID memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        return new MemberInfoResponseDTO(
                member.getId(),
                member.getNickname()
        );
    }
}
