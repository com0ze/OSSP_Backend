package com.example.OSSP_BackEnd.dto.auth;

public record TokenResponse(
    String grantType,
    String accessToken,
    String refreshToken
) {}
