package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 알림 받기 ON/OFF 변경 요청 DTO
 */
public record DutyUpdateRequestDto(
        @NotNull(message = "알림 받기 설정값은 필수입니다")
        Boolean isOnDuty
) {
}
