package com.KimZo2.Back.domain.auth.info;

import java.util.Map;

public class GithubOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        // 깃허브 ID도 숫자일 수 있으므로 안전하게 변환
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    public String getEmail() {
        // 깃허브는 이메일을 비공개로 설정하면 null로 올 수 있음
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        // 깃허브는 name이 없으면 login을 닉네임처럼 사용
        String name = (String) attributes.get("name");
        if (name == null) {
            return (String) attributes.get("login");
        }
        return name;
    }
}
