package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.ReviewCreateRequestDto;
import com.example.OSSP_BackEnd.dto.response.ApiResponse;
import com.example.OSSP_BackEnd.dto.response.ReviewResponseDto;
import com.example.OSSP_BackEnd.security.CustomUserDetails; // CustomUserDetails import 추가
import com.example.OSSP_BackEnd.entity.UserReview;
import com.example.OSSP_BackEnd.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성 API
     * @param dto 리뷰 생성 요청 DTO
     * @return 성공 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createReview(@RequestBody @Valid ReviewCreateRequestDto dto) {
        // TODO: Spring Security 도입 시, Principal 객체나 @AuthenticationPrincipal을 통해 현재 로그인한 사용자 ID를 가져와야 합니다.
        // 예: UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Long reviewerId = ((CustomUserDetails) userDetails).getId();
        // Long reviewerId = 1L; // 임시로 사용하는 하드코딩된 사용자 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long reviewerId = userDetails.getId();

        reviewService.createReview(dto, reviewerId);

        // API 명세에 따라 status 200과 성공 메시지만 반환합니다.
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "리뷰가 성공적으로 등록되었습니다."
        ));
    }

    /**
     * 자신이 작성한 리뷰 목록 조회 API
     * @return 자신이 작성한 리뷰 목록
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getMyReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long reviewerId = userDetails.getId();

        List<ReviewResponseDto> reviews = reviewService.getReviewsByReviewerId(reviewerId);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "자신이 작성한 리뷰 목록입니다.",
                reviews
        ));
    }
}