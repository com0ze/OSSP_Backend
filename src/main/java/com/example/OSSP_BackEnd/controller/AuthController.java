package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.auth.LoginRequest;
import com.example.OSSP_BackEnd.dto.auth.SignUpRequest;
import com.example.OSSP_BackEnd.dto.auth.TokenRefreshRequest;
import com.example.OSSP_BackEnd.dto.auth.TokenResponse;
import com.example.OSSP_BackEnd.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 신규 사용자 회원가입을 처리합니다.
     * 성공 시 201 Created 상태 코드를 반환합니다.
     * @param signUpRequest 회원가입 요청 DTO (email, password, nickname)
     * @return ResponseEntity<Void>
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 사용자 로그인을 처리하고, 성공 시 JWT 토큰을 발급합니다.
     *
     * @param loginRequest 로그인 요청 DTO (email, password)
     * @return 성공 시, Access Token과 Refresh Token을 포함한 ResponseEntity
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 유효한 Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
     *
     * @param refreshRequest 토큰 재발급 요청 DTO (refreshToken)
     * @return 성공 시, 새로운 Access Token을 포함한 ResponseEntity
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        TokenResponse tokenResponse = authService.refresh(refreshRequest);
        return ResponseEntity.ok(tokenResponse);
    }
}