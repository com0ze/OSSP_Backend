package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 위치 정보 갱신 요청 DTO (경량화 버전)
 * 프론트엔드에서 지오펜싱 처리 후 건물명만 전달
 */
public record LocationUpdateRequestDto(
        @NotBlank(message = "건물 정보는 필수입니다")
        String currentBuilding
) {
}
