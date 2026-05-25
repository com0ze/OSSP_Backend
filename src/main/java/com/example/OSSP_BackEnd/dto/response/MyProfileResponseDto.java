package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 현재 로그인한 사용자의 상세 프로필 정보 조회를 위한 응답 DTO
 * 보안상 위험한 정보(password, tokens)는 철저히 배제하고 화면 렌더링에 필요한 정보만 담습니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyProfileResponseDto {

    private Long userId;
    private String nickname;
    private String email;
    private BigDecimal mannerScore;
    private String currentBuilding;
    private Boolean isOnDuty;
    private String role;
    private LocalDateTime createdAt; // (선택) 가입일 표시용

    public static MyProfileResponseDto from(User user) {
        return MyProfileResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .mannerScore(user.getMannerScore())
                .currentBuilding(user.getCurrentBuilding())
                .isOnDuty(user.getIsOnDuty())
                .role(user.getRole().name()) // Enum 타입일 경우 문자열로 변환
                .createdAt(user.getCreatedAt())
                .build();
    }
}