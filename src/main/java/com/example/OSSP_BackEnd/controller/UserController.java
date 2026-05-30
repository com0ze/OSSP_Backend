package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.DeviceTokenRequestDto;
import com.example.OSSP_BackEnd.dto.request.DutyUpdateRequestDto;
import com.example.OSSP_BackEnd.dto.request.LocationUpdateRequestDto;
import com.example.OSSP_BackEnd.dto.response.ApiResponse;
import com.example.OSSP_BackEnd.dto.response.MyProfileResponseDto;
import com.example.OSSP_BackEnd.dto.response.UserProfileResponseDto;
import com.example.OSSP_BackEnd.dto.response.UserReviewResponseDto;
import com.example.OSSP_BackEnd.security.CustomUserDetails;
import com.example.OSSP_BackEnd.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 유저 위치 정보 갱신 API
     * 프론트엔드에서 지오펜싱 처리 후 건물명 전달
     *
     * PATCH /api/v1/users/location
     */
    @PatchMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LocationUpdateRequestDto dto
    ) {
        userService.updateLocation(userDetails.getId(), dto);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "위치 정보가 성공적으로 갱신되었습니다.")
        );
    }

    /**
     * FCM 기기 토큰 등록 API
     * 푸시 알림을 위한 기기 토큰 저장
     *
     * PATCH /api/v1/users/me/device-token
     */
    @PatchMapping("/me/device-token")
    public ResponseEntity<ApiResponse<Void>> updateDeviceToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeviceTokenRequestDto dto
    ) {
        userService.updateDeviceToken(userDetails.getId(), dto);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "기기 토큰이 성공적으로 등록되었습니다.")
        );
    }

    /**
     * 알림 받기 ON/OFF 변경 API
     * 대여 요청 알림 수신 여부 설정
     *
     * PATCH /api/v1/users/me/duty
     */
    @PatchMapping("/me/duty")
    public ResponseEntity<ApiResponse<Void>> updateDutyStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DutyUpdateRequestDto dto
    ) {
        userService.updateDutyStatus(userDetails.getId(), dto);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "알림 받기 설정이 성공적으로 변경되었습니다.")
        );
    }

    /**
     * 특정 유저 프로필 조회 API
     * 대기열 목록에서 유저를 클릭했을 때 상세 정보 반환
     * 민감한 정보는 제외하고 안전한 정보만 반환
     *
     * GET /api/v1/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getUserProfile(
            @PathVariable Long userId
    ) {
        UserProfileResponseDto profile = userService.getUserProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "유저 프로필을 성공적으로 조회했습니다.", profile)
        );
    }

    /**
     * [GET] /api/v1/users/me
     * 현재 로그인한 사용자의 프로필 정보(닉네임, 매너 점수)를 조회하는 API
     * HTTP Header에 사용자 인증 토큰(Authorization: Bearer {token})이 포함되어야 합니다.
     *
     * @return ResponseEntity<ApiResponse<MyProfileResponseDto>> 현재 로그인한 사용자의 프로필 정보
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponseDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyProfileResponseDto myProfile = userService.getMyProfile(userDetails.getId());

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "내 프로필 정보를 성공적으로 조회했습니다.", myProfile)
        );
    }

    /**
     * [GET] /api/v1/users/me/reviews
     * 현재 로그인한 유저가 받은 리뷰 목록을 조회하는 API (마이페이지용)
     * 최신순으로 정렬됩니다.
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @return ResponseEntity<ApiResponse<List<UserReviewResponseDto>>> 리뷰 목록
     */
    @GetMapping("/me/reviews")
    public ResponseEntity<ApiResponse<List<UserReviewResponseDto>>> getMyReceivedReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserReviewResponseDto> reviews = userService.getMyReceivedReviews(
                userDetails.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "내가 받은 리뷰 목록을 성공적으로 조회했습니다.",
                        reviews
                )
        );
    }

    /**
     * [GET] /api/v1/users/{userId}/reviews
     * 특정 유저가 받은 리뷰 목록을 조회하는 API (타인 프로필용)
     * 최신순으로 정렬됩니다.
     *
     * @param userId 조회할 사용자 ID
     * @return ResponseEntity<ApiResponse<List<UserReviewResponseDto>>> 리뷰 목록
     */
    @GetMapping("/{userId}/reviews")
    public ResponseEntity<ApiResponse<List<UserReviewResponseDto>>> getUserReceivedReviews(
            @PathVariable Long userId
    ) {
        List<UserReviewResponseDto> reviews = userService.getUserReceivedReviews(
                userId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "유저가 받은 리뷰 목록을 성공적으로 조회했습니다.",
                        reviews
                )
        );
    }
}