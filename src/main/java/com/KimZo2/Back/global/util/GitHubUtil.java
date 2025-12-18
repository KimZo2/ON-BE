package com.KimZo2.Back.global.util;

import com.KimZo2.Back.domain.dto.auth.GitHubDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubUtil {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.github.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.github.user-info-uri}")
    private String profileUri;

    private final WebClient.Builder webClientBuilder;

    // 액세스 토큰 요청
    public String requestToken(String accessCode) {
        WebClient webClient = webClientBuilder.build();

        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(
                        new GitHubDTO.GithubTokenRequest(
                                clientId,
                                clientSecret,
                                accessCode,
                                redirectUri
                        )
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).doOnNext(errorBody ->
                                log.error("깃허브 토큰 요청 실패: {}", errorBody)
                        ).then(Mono.error(new RuntimeException("토큰 요청 실패")))
                )
                .bodyToMono(JsonNode.class)
                .map(tokenResponse -> tokenResponse.get("access_token").asText())
                .block();
    }
    // 사용자 정보 요청
    public GitHubDTO.GithubUser requestProfile(String accessToken) {
        WebClient webClient = webClientBuilder.build();

        return webClient.get()
                .uri(profileUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GitHubDTO.GithubUser.class)
                .block();
    }
}
