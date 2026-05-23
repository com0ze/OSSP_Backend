package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.config.JwtTokenProvider;
import com.example.OSSP_BackEnd.dto.auth.LoginRequest;
import com.example.OSSP_BackEnd.dto.auth.TokenRefreshRequest;
import com.example.OSSP_BackEnd.dto.auth.TokenResponse;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 사용자 로그인 처리
     * 1. 이메일/비밀번호로 인증 시도
     * 2. 인증 성공 시, Access/Refresh 토큰 생성
     * 3. Refresh 토큰을 DB에 저장
     * 4. 토큰 반환
     */
    public TokenResponse login(LoginRequest request) {
        // 1. AuthenticationManager를 사용하여 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. 인증된 정보를 기반으로 JWT 토큰 생성
        JwtTokenProvider.TokenResponse tokenDto = jwtTokenProvider.generateToken(authentication);

        // 3. DB에서 사용자 정보를 찾아 Refresh Token 업데이트
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateRefreshToken(tokenDto.refreshToken());
        userRepository.save(user);

        // 4. 생성된 토큰 응답 반환
        return new TokenResponse(tokenDto.grantType(), tokenDto.accessToken(), tokenDto.refreshToken());
    }

    /**
     * Refresh Token을 이용한 Access Token 재발급
     * 1. Refresh Token 유효성 검증
     * 2. DB에서 해당 토큰을 가진 사용자 조회
     * 3. 새로운 Access Token 생성 후 반환
     */
    @Transactional(readOnly = true)
    public TokenResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        // 1. Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. DB에서 리프레시 토큰으로 사용자 정보 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UsernameNotFoundException("리프레시 토큰에 해당하는 사용자를 찾을 수 없습니다."));

        // 3. 새로운 Access Token 생성
        // 기존 Authentication 객체를 다시 만들어서 토큰 생성을 위임합니다.
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(user.getUsername(), "", user.getAuthorities()),
                null,
                user.getAuthorities()
        );

        JwtTokenProvider.TokenResponse tokenDto = jwtTokenProvider.generateToken(authentication);

        // Refresh Token은 그대로 두고 Access Token만 새로 발급하여 반환
        return new TokenResponse(tokenDto.grantType(), tokenDto.accessToken(), refreshToken);
    }
}
