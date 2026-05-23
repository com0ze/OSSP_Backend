package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.ReviewCreateRequestDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.entity.UserReview;
import com.example.OSSP_BackEnd.exception.InvalidRequestStateException;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import com.example.OSSP_BackEnd.repository.UserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final UserReviewRepository userReviewRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final UserRepository userRepository;
    private static final BigDecimal ALPHA = new BigDecimal("0.1");

    /**
     * 리뷰를 생성하고, 평가받는 사용자의 매너 점수를 업데이트하는 핵심 비즈니스 로직을 수행합니다.
     *
     * @param dto 리뷰 생성에 필요한 데이터 (매칭 ID, 점수, 코멘트)
     * @param reviewerId 리뷰를 작성하는 사용자의 ID (Security Context에서 가져옴)
     * @return 생성된 UserReview 엔티티
     */
    @Transactional
    public UserReview createReview(ReviewCreateRequestDto dto, Long reviewerId) {
        // 1. DTO의 matchId를 사용하여 매칭 기록을 조회합니다. 없으면 예외를 발생시킵니다.
        MatchHistory matchHistory = matchHistoryRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("매칭 기록을 찾을 수 없습니다."));

        // 2. 매칭된 거래가 'COMPLETED'(거래 완료) 상태인지 확인합니다. 아니라면 예외를 발생시킵니다.
        CallRequest callRequest = matchHistory.getRequest();
        if (callRequest.getStatus() != RequestStatus.COMPLETED) {
            throw new InvalidRequestStateException("거래가 완료되지 않은 요청에 대한 리뷰는 작성할 수 없습니다.");
        }

        // 3. 리뷰어(평가자) 엔티티를 조회합니다.
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰어 정보를 찾을 수 없습니다."));

        // 4. 이미 해당 매칭에 대해 리뷰를 작성했는지 확인합니다. 중복 작성 시 예외를 발생시킵니다.
        if (userReviewRepository.existsByMatchHistoryAndReviewer(matchHistory, reviewer)) {
            throw new InvalidRequestStateException("이미 이 거래에 대한 리뷰를 작성했습니다.");
        }

        // 5. 리뷰어(reviewer)가 수요자(requester)인지 공급자(provider)인지 확인하여 평가받는 사람(reviewee)을 결정합니다.
        User requester = callRequest.getRequester();
        User provider = matchHistory.getProvider();
        User reviewee;

        if (reviewerId.equals(requester.getId())) {
            // 리뷰어가 수요자이면, 평가받는 사람은 공급자입니다.
            reviewee = provider;
        } else if (reviewerId.equals(provider.getId())) {
            // 리뷰어가 공급자이면, 평가받는 사람은 수요자입니다.
            reviewee = requester;
        } else {
            // 리뷰어가 해당 거래의 당사자가 아니면 예외를 발생시킵니다.
            throw new InvalidRequestStateException("해당 거래의 당사자만 리뷰를 작성할 수 있습니다.");
        }

        // 6. UserReview 엔티티를 생성하고 데이터베이스에 저장합니다.
        UserReview userReview = UserReview.create(
                reviewer,
                reviewee,
                dto.getScore(),
                dto.getComments(),
                matchHistory
        );
        userReviewRepository.save(userReview);

        // 7. 평가받은 사용자(reviewee)의 매너 점수를 업데이트합니다.
        updateMannerScore(reviewee, dto.getScore());

        return userReview;
    }

    /**
     * 특정 사용자의 매너 점수를 다시 계산하여 업데이트합니다.
     * @param reviewee 평가받은 사용자
     * @param newScore 새로 받은 리뷰 점수
     */
    private void updateMannerScore(User reviewee, BigDecimal newScore) {
        BigDecimal currentScore = reviewee.getMannerScore();
        if (currentScore == null) {
            currentScore = new BigDecimal("3.0");
        }

        // 새로운 점수 = (α * 새로운 리뷰 점수) + ((1 - α) * 기존 점수)
        BigDecimal updatedScore = newScore.multiply(ALPHA)
                .add(currentScore.multiply(BigDecimal.ONE.subtract(ALPHA)));

        // 소수점 첫째 자리에서 반올림
        BigDecimal newMannerScore = updatedScore.setScale(1, RoundingMode.HALF_UP);

        reviewee.updateMannerScore(newMannerScore);
    }
}