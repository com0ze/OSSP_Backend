package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.ReviewCreateRequestDto;
import com.example.OSSP_BackEnd.dto.response.ApiResponse;
import com.example.OSSP_BackEnd.dto.response.ReviewResponseDto;
import com.example.OSSP_BackEnd.entity.UserReview;
import com.example.OSSP_BackEnd.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(@RequestBody @Valid ReviewCreateRequestDto dto) {
        UserReview userReview = reviewService.createReview(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED,
                "리뷰가 성공적으로 작성되었습니다.",
                ReviewResponseDto.of(userReview)
        ));
    }
}
