package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.UserReview;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private Long matchId;
    private Long reviewerId;
    private Long revieweeId;
    private BigDecimal score;
    private String comments;
    private LocalDateTime createdAt;

    public static ReviewResponseDto of(UserReview userReview) {
        return ReviewResponseDto.builder()
                .reviewId(userReview.getId())
                .matchId(userReview.getMatchHistory().getId())
                .reviewerId(userReview.getReviewer().getId())
                .revieweeId(userReview.getReviewee().getId())
                .score(userReview.getScore())
                .comments(userReview.getComments())
                .createdAt(userReview.getCreatedAt())
                .build();
    }
}
