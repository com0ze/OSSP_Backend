package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 위치 정보 갱신 요청 DTO
 */
public record LocationUpdateRequestDto(
        @NotNull(message = "위도는 필수입니다")
        Double latitude,

        @NotNull(message = "경도는 필수입니다")
        Double longitude
) {
}
