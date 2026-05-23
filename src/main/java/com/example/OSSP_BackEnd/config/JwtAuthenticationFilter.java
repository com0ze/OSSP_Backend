package com.example.OSSP_BackEnd.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터.
 * HTTP 요청의 'Authorization' 헤더에서 JWT 토큰을 추출하여 유효성을 검사하고,
 * 유효한 경우 Spring Security의 SecurityContext에 인증 정보를 설정합니다.
 * 이 필터는 각 요청마다 한 번씩 실행됩니다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request Header에서 토큰을 꺼냅니다.
        String jwt = resolveToken(request);

        // 2. validateToken으로 토큰 유효성 검사를 하고, 정상이면 Authentication 객체를 가져옵니다.
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
            // 3. SecurityContext에 Authentication 객체를 저장합니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HttpServletRequest에서 'Authorization' 헤더를 파싱하여 JWT 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열 (헤더가 없거나 'Bearer '로 시작하지 않으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
