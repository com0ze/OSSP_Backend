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
    private String revieweeNickname; // 리뷰 받은 사람의 닉네임
    private BigDecimal score;
    private String comments;
    private LocalDateTime createdAt;

    public static ReviewResponseDto of(UserReview userReview) {
        return ReviewResponseDto.builder()
                .reviewId(userReview.getId())
                .matchId(userReview.getMatchHistory().getId())
                .revieweeNickname(userReview.getReviewee().getNickname())
                .score(userReview.getScore())
                .comments(userReview.getComments())
                .createdAt(userReview.getCreatedAt())
                .build();
    }
}