package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 현재 로그인한 사용자의 프로필 정보 조회를 위한 응답 DTO 클래스.
 * API 명세에 따라 닉네임과 매너 점수만 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyProfileResponseDto {

    private String nickname;
    private BigDecimal mannerScore;

    /**
     * User 엔티티를 MyProfileResponseDto로 변환하는 정적 팩토리 메서드.
     * User 엔티티에서 필요한 정보(닉네임, 매너 점수)만 추출하여 DTO를 생성합니다.
     *
     * @param user User 엔티티 객체
     * @return MyProfileResponseDto 객체
     */
    public static MyProfileResponseDto from(User user) {
        return MyProfileResponseDto.builder()
                .nickname(user.getNickname())
                .mannerScore(user.getMannerScore())
                .build();
    }
}
