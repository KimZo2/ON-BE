package com.KimZo2.Back.domain.auth.dto;


import lombok.Getter;


public class KakaoDTO {
    @Getter
    public static class OAuthToken {
        private String token_type;
        private String access_token;
        private String id_token;
        private Integer expires_in;
        private String refresh_token;
        private Integer refresh_token_expires_in;
        private String scope;
    }


    @Getter
    public static class KakaoProfile {
        private Long id;
    }
}
