package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.DeviceTokenRequestDto;
import com.example.OSSP_BackEnd.dto.request.DutyUpdateRequestDto;
import com.example.OSSP_BackEnd.dto.request.LocationUpdateRequestDto;
import com.example.OSSP_BackEnd.dto.response.ApiResponse;
import com.example.OSSP_BackEnd.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody LocationUpdateRequestDto dto
    ) {
        // TODO: 시큐리티 인증 구현 후 실제 로그인한 유저 ID로 변경
        Long currentUserId = 1L; // 임시 하드코딩

        userService.updateLocation(currentUserId, dto);

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
            @Valid @RequestBody DeviceTokenRequestDto dto
    ) {
        // TODO: 시큐리티 인증 구현 후 실제 로그인한 유저 ID로 변경
        Long currentUserId = 1L; // 임시 하드코딩

        userService.updateDeviceToken(currentUserId, dto);

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
            @Valid @RequestBody DutyUpdateRequestDto dto
    ) {
        // TODO: 시큐리티 인증 구현 후 실제 로그인한 유저 ID로 변경
        Long currentUserId = 1L; // 임시 하드코딩

        userService.updateDutyStatus(currentUserId, dto);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "알림 받기 설정이 성공적으로 변경되었습니다.")
        );
    }
}
