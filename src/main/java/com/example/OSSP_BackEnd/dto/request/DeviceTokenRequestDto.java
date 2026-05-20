package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * FCM 기기 토큰 등록 요청 DTO
 */
public record DeviceTokenRequestDto(
        @NotBlank(message = "FCM 토큰은 필수입니다")
        String fcmToken
) {
}
