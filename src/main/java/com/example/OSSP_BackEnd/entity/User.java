package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "manner_score", precision = 3, scale = 1)
    private BigDecimal mannerScore;

    @Column(name = "is_on_duty")
    private Boolean isOnDuty;

    @Column(name = "current_building", length = 50)
    private String currentBuilding;

    @Column(name = "device_token", length = 255)
    private String deviceToken;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.mannerScore = BigDecimal.valueOf(0.0);
        this.isOnDuty = false;
    }

    // 건물 정보 업데이트 메서드 (경량화 버전)
    public void updateCurrentBuilding(String currentBuilding) {
        this.currentBuilding = currentBuilding;
    }

    // FCM 기기 토큰 업데이트 메서드
    public void updateDeviceToken(String fcmToken) {
        this.deviceToken = fcmToken;
    }

    // 매너 점수 업데이트 메서드
    public void updateMannerScore(BigDecimal mannerScore) {
        this.mannerScore = mannerScore;
    } // 💡 이 닫는 괄호가 빠져 있었습니다!

    // 알림 받기 ON/OFF 상태 업데이트 메서드
    public void updateDutyStatus(Boolean isOnDuty) {
        this.isOnDuty = isOnDuty;
    }

    // userId 편의 메서드 (다른 팀원 코드가 깨지지 않게 방어)
    public Long getUserId() {
        return this.id;
    }
}