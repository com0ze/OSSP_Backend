package com.example.OSSP_BackEnd.controller;

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
     * Ray-Casting 알고리즘으로 건물 내부 여부 판별
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
}
