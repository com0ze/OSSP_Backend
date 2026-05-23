package com.example.OSSP_BackEnd.config;

import com.example.OSSP_BackEnd.security.CustomUserDetails; // CustomUserDetails import 추가
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // 더 이상 직접 사용하지 않지만, 기존 코드와의 비교를 위해 남겨둠
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidity,
                            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidity) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidity * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidity * 1000;
    }

    /**
     * 인증 객체를 기반으로 Access Token과 Refresh Token을 생성합니다.
     *
     * @param authentication Spring Security의 인증 정보
     * @return 생성된 토큰 정보를 담은 TokenResponse DTO
     */
    public TokenResponse generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // CustomUserDetails에서 userId를 가져와 클레임에 추가
        Long userId = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        } else {
            // CustomUserDetails가 아닌 경우 (예: 테스트 또는 다른 인증 방식),
            // authentication.getName()을 userId로 사용하거나 예외 처리
            // 여기서는 일단 Long으로 변환 가능한 경우에만 사용하도록 처리
            try {
                userId = Long.valueOf(authentication.getName());
            } catch (NumberFormatException e) {
                // userId를 클레임에 추가하지 않거나, 다른 방식으로 처리
            }
        }


        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds);
        String accessToken = Jwts.builder()
                .subject(authentication.getName()) // 이메일 (username)
                .claim("auth", authorities)
                .claim("userId", userId) // userId 클레임 추가
                .issuedAt(new Date())
                .expiration(accessTokenExpiresIn)
                .signWith(key)
                .compact();

        // Refresh Token 생성
        Date refreshTokenExpiresIn = new Date(now + refreshTokenValidityInMilliseconds);
        String refreshToken = Jwts.builder()
                .expiration(refreshTokenExpiresIn)
                .signWith(key)
                .compact();

        return new TokenResponse("Bearer", accessToken, refreshToken);
    }
    
    /**
     * 주어진 Access Token을 복호화하여 인증 객체를 반환합니다.
     *
     * @param accessToken 암호화된 Access Token 문자열
     * @return Spring Security가 사용하는 인증(Authentication) 객체
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // userId 클레임 추출
        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();

        // CustomUserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new CustomUserDetails(userId, email, authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 토큰 문자열
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // MalformedJwtException, ExpiredJwtException, UnsupportedJwtException, IllegalArgumentException
            return false;
        }
    }

    /**
     * 토큰에서 클레임 정보를 추출합니다.
     *
     * @param accessToken 클레임을 추출할 토큰
     * @return 추출된 클레임
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (Exception e) {
            // 토큰 만료 등 예외 처리
            return Jwts.claims().build();
        }
    }

    // DTO를 내부 클래스로 정의하거나 별도 파일로 분리할 수 있습니다.
    public record TokenResponse(String grantType, String accessToken, String refreshToken) {}
}