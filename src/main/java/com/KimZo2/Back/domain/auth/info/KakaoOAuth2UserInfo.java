package com.KimZo2.Back.domain.auth.info;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo{
    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> kakaoProfile;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getProviderId() {
        // 카카오의 ID는 Long 타입으로 오므로 String으로 변환
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        // 이메일 동의를 안 했을 수도 있으므로 null 체크 필요
        if (kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        if (kakaoProfile == null) {
            return null;
        }
        return (String) kakaoProfile.get("nickname");
    }
}
