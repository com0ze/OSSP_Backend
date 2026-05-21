package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 유저 프로필 조회 응답 DTO
 * 민감한 정보(password, deviceToken, currentBuilding 등)는 제외하고 안전한 정보만 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDto {
    
    private Long userId;
    private String nickname;
    private String email;
    private BigDecimal mannerScore;

    /**
     * User 엔티티를 UserProfileResponseDto로 변환하는 정적 팩토리 메서드
     * 
     * @param user User 엔티티
     * @return UserProfileResponseDto
     */
    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .mannerScore(user.getMannerScore())
                .build();
    }
}
