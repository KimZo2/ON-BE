package com.KimZo2.Back.global.util;

import com.KimZo2.Back.domain.auth.dto.KakaoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class KakaoUtil {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String authorization_code;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String profileUri;


    public KakaoDTO.OAuthToken requestToken(String accessCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", authorization_code);
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", accessCode);

        WebClient webClient = WebClient.builder()
                .baseUrl(tokenUri)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();


        KakaoDTO.OAuthToken oAuthToken = webClient.post()
                .uri("")
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).doOnNext(errorBody ->
                                log.error("카카오 토큰 요청 실패: {}", errorBody)
                        ).then(Mono.error(new RuntimeException("토큰 요청 실패")))
                )
                .bodyToMono(KakaoDTO.OAuthToken.class)
                .block();

//            if (oAuthToken == null || oAuthToken.getAccess_token() == null) {
//                throw new RuntimeException("카카오 Access Token 발급 실패");
//            }
        log.info("Access Token: {}", oAuthToken.getAccess_token());

        return oAuthToken;

    }


    public KakaoDTO.KakaoProfile requestProfile(KakaoDTO.OAuthToken oAuthToken) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oAuthToken.getAccess_token())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        KakaoDTO.KakaoProfile kakaoProfile = webClient.get()
                .uri("/v2/user/me")
                .retrieve()
                .bodyToMono(KakaoDTO.KakaoProfile.class)
                .block();

        if (kakaoProfile == null) {
            throw new RuntimeException("카카오 프로필 조회 실패");
        }

        return kakaoProfile;
    }
}

