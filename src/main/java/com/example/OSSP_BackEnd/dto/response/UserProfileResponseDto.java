package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 특정 유저 프로필 조회 응답 DTO (타인 조회용)
 * 민감한 개인정보(email, password, isOnDuty 등)를 완전히 제외한 안전한 식별 정보만 반환합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDto {
    
    private Long userId;
    private String nickname;
    private BigDecimal mannerScore;
    
    // 🚨 여기서 email 필드와 매핑 코드를 완전히 삭제했습니다! (IDOR 보안 취약점 차단)

    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .mannerScore(user.getMannerScore())
                .build();
    }
}