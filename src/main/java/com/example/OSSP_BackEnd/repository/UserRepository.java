package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // 💡 추가됨
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional; // 💡 추가됨

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
    
    /**
     * 매칭 알고리즘: 특정 건물에 위치하고 알림 받기 상태가 ON인 유저 조회
     * @param buildingName 건물 이름
     * @return 조건에 맞는 유저 리스트
     */
    @Query("SELECT u FROM User u WHERE u.currentBuilding = :buildingName AND u.isOnDuty = true AND u.fcmToken IS NOT NULL")
    List<User> findActiveUsersInBuilding(@Param("buildingName") String buildingName);
    
    /**
     * 매칭 알고리즘: 여러 건물에 위치하고 알림 받기 상태가 ON인 유저 조회
     * @param buildingNames 건물 이름 리스트
     * @return 조건에 맞는 유저 리스트
     */
    @Query("SELECT u FROM User u WHERE u.currentBuilding IN :buildingNames AND u.isOnDuty = true AND u.fcmToken IS NOT NULL")
    List<User> findActiveUsersInBuildings(@Param("buildingNames") List<String> buildingNames);
    
    /**
     * 스케줄러: 마지막 활동 시간이 특정 일자 이전인 유저 조회 (잠수 유저 차단용)
     * @param cutoffDate 기준 날짜 (5일 전 등)
     * @return 조건에 맞는 유저 리스트
     */
    @Query("SELECT u FROM User u WHERE u.lastActiveAt < :cutoffDate AND u.isOnDuty = true")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 💡 [추가된 핵심 로직]
     * 유저 활동 시 마지막 활동 시간(lastActiveAt)을 초고속으로 업데이트합니다.
     * 데이터베이스 부하를 최소화하기 위해 엔티티 전체를 SELECT 해오지 않고 직접 UPDATE 명령을 날립니다.
     */
    @Modifying(clearAutomatically = true) // 영속성 컨텍스트의 데이터 꼬임을 방지하기 위해 자동 플러시/클리어 설정
    @Transactional
    @Query("UPDATE User u SET u.lastActiveAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastActiveAt(@Param("userId") Long userId);
}