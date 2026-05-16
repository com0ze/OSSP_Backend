package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestCreateDto(
        @NotBlank(message = "물품명은 필수입니다.")
        @Size(max = 100, message = "물품명은 100자 이하여야 합니다.")
        String itemName,

        @NotBlank(message = "건물명은 필수입니다.")
        @Size(max = 100, message = "건물명은 100자 이하여야 합니다.")
        String buildingName,

        @Size(max = 50, message = "보상 금액은 50자 이하여야 합니다.")
        String rewardAmt,

        @Size(max = 50, message = "대여 시간은 50자 이하여야 합니다.")
        String duration,

        @Size(max = 255, message = "메모는 255자 이하여야 합니다.")
        String memo,

        @NotNull(message = "요청자 ID는 필수입니다.")
        Long requesterId
) {
}