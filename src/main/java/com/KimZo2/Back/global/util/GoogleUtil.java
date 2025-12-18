package com.KimZo2.Back.global.util;

import com.KimZo2.Back.domain.auth.dto.GoogleDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleUtil {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String profileUri;

    @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
    private String authorization_code;

    private final WebClient.Builder webClientBuilder;

    //  Access Token 요청
    public String requestToken(String accessCode) {
        WebClient webClient = webClientBuilder.build();

        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .bodyValue("code=" + accessCode
                        + "&client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + redirectUri
                        + "&grant_type=" +authorization_code)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(tokenResponse -> tokenResponse.get("access_token").asText())
                .block();
    }

    // 사용자 정보 요청
    public GoogleDTO.GoogleUser requestProfile(String accessToken) {
        WebClient webClient = webClientBuilder.build();

        return webClient.get()
                .uri(profileUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleDTO.GoogleUser.class)
                .block();
    }
}
