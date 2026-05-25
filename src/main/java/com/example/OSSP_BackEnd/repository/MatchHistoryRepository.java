package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchHistoryRepository extends JpaRepository<MatchHistory, Long> {

    @Query("SELECT mh FROM MatchHistory mh JOIN FETCH mh.request WHERE mh.request.id = :requestId")
    Optional<MatchHistory> findByRequestIdWithRequest(Long requestId);

    // 기존 메서드 유지
    Optional<MatchHistory> findByRequest_Id(Long requestId);
    
    /**
     * 가중치 계산: 특정 유저의 최근 7일 내 공급 횟수 조회
     * @param providerId 공급자 ID
     * @param since 기준 일자 (7일 전)
     * @return 공급 횟수
     */
    @Query("SELECT COUNT(mh) FROM MatchHistory mh WHERE mh.provider.id = :providerId AND mh.matchedAt >= :since AND mh.returnedAt IS NOT NULL")
    long countRecentCompletedProvides(@Param("providerId") Long providerId, @Param("since") LocalDateTime since);
    
    /**
     * 가중치 계산: 특정 유저의 총 공급 이력 횟수 조회 (신규 유저 판별용)
     * @param providerId 공급자 ID
     * @return 총 공급 횟수
     */
    @Query("SELECT COUNT(mh) FROM MatchHistory mh WHERE mh.provider.id = :providerId")
    long countTotalProvides(@Param("providerId") Long providerId);
    
    /**
     * 가중치 계산: 특정 유저가 특정 물건을 빌려준 이력이 있는지 확인
     * @param providerId 공급자 ID
     * @param itemName 물건 이름
     * @return 해당 물건 대여 이력이 있으면 true
     */
    @Query("SELECT CASE WHEN COUNT(mh) > 0 THEN true ELSE false END FROM MatchHistory mh " +
           "JOIN mh.request r WHERE mh.provider.id = :providerId AND r.itemName = :itemName")
    boolean hasProvidedItem(@Param("providerId") Long providerId, @Param("itemName") String itemName);
}
