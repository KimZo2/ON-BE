package com.KimZo2.Back.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
public class GitHubDTO {

    @Getter
    @AllArgsConstructor
    public static class GithubTokenRequest {
        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("client_secret")
        private String clientSecret;

        private String code;

        @JsonProperty("redirect_uri")
        private String redirectUri;
    }

    @Getter
    @Setter
    public static class GithubUser {
        private Long id;              // providerId로 사용
        private String login;         // 깃허브 ID
        private String name;          // 사용자 이름
        private String email;         // 이메일 (비어있을 수 있음)
        private String avatar_url;    // 프로필 이미지 URL
    }
}
