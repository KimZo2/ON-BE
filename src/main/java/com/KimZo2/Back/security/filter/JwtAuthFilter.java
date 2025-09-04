package com.KimZo2.Back.security.filter;

import com.KimZo2.Back.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    public static final List<String> allowUrls = List.of(
            "/auth/**",
//            "/room",
//            "/room/**",
            "/swagger-ui/**","/api-docs", "/swagger-ui-custom.html",
            "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html"
    );

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : allowUrls) {
            if (pathMatcher.match(pattern, path)) return true;
        }
        return false;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // CORS 정책 패싱
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 이미 인증이 존재
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.resolveBearer(request.getHeader("Authorization"));

        // 토큰이 존재하지 않음
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        //토큰 존재, 검증
        if (!jwtUtil.validate(token) || jwtUtil.isExpired(token)) {
            writeUnauthorized(response, "INVALID_OR_EXPIRED_TOKEN");
            return; // 더 진행하지 않음
        }

        String userId = jwtUtil.getUserId(token);
        var auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"code\":\"" + code + "\",\"message\":\"Unauthorized\"}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}
