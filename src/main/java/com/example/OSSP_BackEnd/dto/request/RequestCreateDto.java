package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public record RequestCreateDto(
        @NotBlank(message = "물품명은 필수입니다.")
        @Size(max = 100, message = "물품명은 100자 이하여야 합니다.")
        String itemName,

        @NotBlank(message = "건물명은 필수입니다.")
        @Size(max = 100, message = "건물명은 100자 이하여야 합니다.")
        String buildingName,

        @NotNull(message = "보상 금액은 필수입니다.")
        @Min(value = 0, message = "보상 금액은 0원 이상이어야 합니다.")
        Integer rewardAmt, // 💡 String -> Integer로 변경 및 숫자형 어노테이션으로 교체

        @NotNull(message = "대여 시간은 필수입니다.")
        @Min(value = 1, message = "대여 시간은 최소 1분 이상이어야 합니다.")
        Integer duration, // 💡 String -> Integer로 변경 및 숫자형 어노테이션으로 교체

        @Size(max = 255, message = "메모는 255자 이하여야 합니다.")
        String memo,

        @NotNull(message = "요청자 ID는 필수입니다.")
        Long requesterId
) {
}