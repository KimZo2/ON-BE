package com.KimZo2.Back.global.config;

import com.KimZo2.Back.domain.auth.service.CustomOAuth2UserService;
import com.KimZo2.Back.global.jwt.JwtAccessDeniedHandler;
import com.KimZo2.Back.global.jwt.JwtAuthEntryPoint;
import com.KimZo2.Back.global.jwt.JwtAuthFilter;
import com.KimZo2.Back.global.security.handler.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    private static final String[] AUTH_WHITELIST = {
            "/auth/**",
            "/login/oauth2/**",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/ws/**",
            "/error",
            "/favicon.ico"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP 기본 인증 비활성화

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthEntryPoint)    // 401 (인증 실패)
                        .accessDeniedHandler(jwtAccessDeniedHandler)    // 403 (권한 없음)
                )

                // 경로를 AUTH_WHITELIST로 통합
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(AUTH_WHITELIST).permitAll() // AUTH_WHITELIST 경로는 모두 허용
                        .anyRequest().authenticated() // 나머지 모든 경로는 인증 필요
                )

                .oauth2Login(oauth2 -> oauth2
                        // 로그인 성공 시 처리할 핸들러 (JWT 발급 및 리다이렉트)
                        .successHandler(oAuth2LoginSuccessHandler)

                        // 사용자 정보 가져오는 엔드포인트 설정
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )

                // [유지] 사용자가 제공한 JwtAuthFilter 사용
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // JWT 토큰 사용 시, 자격증명(쿠키 등) 불필요
        configuration.setMaxAge(3600L); // Preflight 요청 캐시 시간 (1시간)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 이 CORS 설정 적용
        return source;
    }
}
