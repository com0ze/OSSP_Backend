package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.NotNull;

public record RequestAcceptRequestDto(
        @NotNull(message = "수락자 ID는 필수입니다.")
        Long providerId
) {
}