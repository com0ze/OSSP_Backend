package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.UserReview;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 특정 유저가 받은 리뷰 정보를 반환하는 DTO
 * (마이페이지 및 타인 프로필 페이지에서 사용)
 */
@Getter
@Builder
public class UserReviewResponseDto {

    /**
     * 리뷰 ID
     */
    private Long reviewId;

    /**
     * 리뷰를 작성한 사람의 닉네임
     */
    private String reviewerNickname;

    /**
     * 평점 (5.0 만점)
     */
    private BigDecimal score;

    /**
     * 후기 한 줄 코멘트
     */
    private String comments;

    /**
     * 리뷰 작성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 어떤 거래(매칭)에 대한 리뷰인지 식별자
     */
    private Long matchId;

    /**
     * Entity → DTO 변환 정적 팩토리 메서드
     *
     * @param userReview UserReview 엔티티
     * @return UserReviewResponseDto
     */
    public static UserReviewResponseDto from(UserReview userReview) {
        return UserReviewResponseDto.builder()
                .reviewId(userReview.getId())
                .reviewerNickname(userReview.getReviewer().getNickname())
                .score(userReview.getScore())
                .comments(userReview.getComments())
                .createdAt(userReview.getCreatedAt())
                .matchId(userReview.getMatchHistory().getId())
                .build();
    }
}
