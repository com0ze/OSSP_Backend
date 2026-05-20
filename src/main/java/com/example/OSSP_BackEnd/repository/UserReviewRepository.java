package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.entity.UserReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Long> {

    /**
     * 매치 기록과 리뷰어를 기반으로 리뷰 존재 여부를 확인합니다.
     * @param matchHistory 매치 기록
     * @param reviewer 리뷰어
     * @return 리뷰 존재 여부
     */
    boolean existsByMatchHistoryAndReviewer(MatchHistory matchHistory, User reviewer);

    /**
     * 특정 사용자가 받은 모든 리뷰의 평균 점수를 계산합니다.
     * @param revieweeId 평가받은 사용자의 ID
     * @return 평균 점수 (리뷰가 없으면 null 반환)
     */
    @Query("SELECT AVG(ur.score) FROM UserReview ur WHERE ur.reviewee.id = :revieweeId")
    Optional<Double> findAverageScoreByRevieweeId(@Param("revieweeId") Long revieweeId);
}
