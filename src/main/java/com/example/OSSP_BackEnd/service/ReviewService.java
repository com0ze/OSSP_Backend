package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.ReviewCreateRequestDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.entity.UserReview;
import com.example.OSSP_BackEnd.exception.InvalidRequestStateException;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import com.example.OSSP_BackEnd.repository.UserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final UserReviewRepository userReviewRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserReview createReview(ReviewCreateRequestDto dto) {
        MatchHistory matchHistory = matchHistoryRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("매칭 기록을 찾을 수 없습니다."));

        CallRequest callRequest = matchHistory.getRequest();
        if (callRequest.getStatus() != RequestStatus.COMPLETED) {
            throw new InvalidRequestStateException("거래가 완료되지 않은 요청에 대한 리뷰는 작성할 수 없습니다.");
        }

        User reviewer = userRepository.findById(dto.getReviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("평가하는 사용자를 찾을 수 없습니다."));

        User reviewee = matchHistory.getProvider(); // 매칭된 제공자가 평가받는 사람이 됨

        UserReview userReview = UserReview.create(
                reviewer,
                reviewee,
                dto.getScore(),
                dto.getComments(),
                matchHistory
        );
        return userReviewRepository.save(userReview);
    }
}
