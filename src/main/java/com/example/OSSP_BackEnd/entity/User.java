package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
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

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

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

    // 위치 정보 업데이트 메서드
    public void updateLocation(Double latitude, Double longitude, String currentBuilding) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentBuilding = currentBuilding;
    }

    // userId 편의 메서드
    public Long getUserId() {
        return this.id;
    }
}
