package com.KimZo2.Back.domain.auth.service;

import com.KimZo2.Back.domain.auth.info.CustomOAuth2User;
import com.KimZo2.Back.domain.auth.info.GithubOAuth2UserInfo;
import com.KimZo2.Back.domain.auth.info.KakaoOAuth2UserInfo;
import com.KimZo2.Back.domain.auth.info.OAuth2UserInfo;
import com.KimZo2.Back.domain.member.repository.MemberRepository;
import com.KimZo2.Back.global.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.KimZo2.Back.global.entity.Role;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 소셜 서비스에서 유저 정보를 가져온다
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}", oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = null;
        if (provider.equals("kakao")) {
            oAuth2UserInfo = new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
        } else if (provider.equals("github")) {
            oAuth2UserInfo = new GithubOAuth2UserInfo(oAuth2User.getAttributes());
        }

        String providerId = oAuth2UserInfo.getProviderId();

        Optional<Member> findMember = memberRepository.findByProviderAndProviderId(provider, providerId);

        Member member;
        if (findMember.isEmpty()) {
            member = Member.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.GUEST)
                    .agreement(false)
                    .build();

            memberRepository.save(member);
        } else {
            member = findMember.get();
        }

        return new CustomOAuth2User(member, oAuth2User.getAttributes());
    }
}
