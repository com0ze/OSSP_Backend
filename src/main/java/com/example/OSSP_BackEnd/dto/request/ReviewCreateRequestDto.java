package com.example.OSSP_BackEnd.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ReviewCreateRequestDto {
    @NotNull(message = "매칭 ID는 필수입니다.")
    private Long matchId;

    @NotNull(message = "평가하는 사용자 ID는 필수입니다.")
    private Long reviewerId;

    @NotNull(message = "점수는 필수입니다.")
    @DecimalMin(value = "0.0", message = "점수는 0.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "점수는 5.0 이하여야 합니다.")
    private BigDecimal score;

    @Size(max = 255, message = "댓글은 255자를 초과할 수 없습니다.")
    private String comments;
}
