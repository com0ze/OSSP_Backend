package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 매칭 알고리즘 가중치 계산 서비스 (디버깅용 로그 강화 버전)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchingScoreService {

    private final MatchHistoryRepository matchHistoryRepository;

    private static final double MANNER_SCORE_WEIGHT = 0.5;  // 50%
    private static final double RECENT_ACTIVITY_WEIGHT = 0.3;  // 30%
    private static final double ITEM_HISTORY_WEIGHT = 0.2;  // 20%

    /**
     * 매칭 파워 스코어 계산 및 상세 분석 로그 출력
     */
    public double calculateMatchingScore(User user, String requestedItemName) {
        double mannerScore = calculateMannerScore(user);
        double recentActivityScore = calculateRecentActivityScore(user);
        double itemHistoryScore = calculateItemHistoryScore(user, requestedItemName);

        double totalScore = (mannerScore * MANNER_SCORE_WEIGHT)
                + (recentActivityScore * RECENT_ACTIVITY_WEIGHT)
                + (itemHistoryScore * ITEM_HISTORY_WEIGHT);

        double finalScore = Math.round(totalScore * 100.0) / 100.0;

        // 🔥 [핵심 개선] log.debug 대신 log.info를 사용하여 모든 유저의 점수 스펙을 콘솔에 강제 출력합니다.
        log.info("   [점수 분석] 유저 #{} ({}) -> 총점: {}점 [매너(50%): {}점, 최근접속(30%): {}점, 대여이력(20%): {}점]",
                user.getId(), user.getNickname(), finalScore, mannerScore, recentActivityScore, itemHistoryScore);

        return finalScore;
    }

    private double calculateMannerScore(User user) {
        if (user.getMannerScore() == null) {
            return 60.0;
        }
        return user.getMannerScore()
                .divide(BigDecimal.valueOf(5.0), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100.0))
                .doubleValue();
    }

    private double calculateRecentActivityScore(User user) {
        if (user.getLastActiveAt() == null) {
            return 0.0; 
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursElapsed = ChronoUnit.HOURS.between(user.getLastActiveAt(), now);

        if (hoursElapsed <= 1) {
            return 100.0;
        }
        if (hoursElapsed >= 168) {
            return 0.0;
        }

        double decayRatio = 1.0 - ((double) hoursElapsed / 168.0);
        return Math.round(decayRatio * 1000.0) / 10.0;
    }

    private double calculateItemHistoryScore(User user, String requestedItemName) {
        // 지난번 추가한 띄어쓰기/대소문자 무시 고도화 쿼리가 있다면 해당 메서드명으로 유지해주세요!
        boolean hasHistory = matchHistoryRepository.hasProvidedItemBefore(user.getId(), requestedItemName);
        return hasHistory ? 100.0 : 0.0;
    }

    public boolean isNewUserWithShield(User user) {
        LocalDateTime dayAgo = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        boolean isWithin24Hours = user.getCreatedAt().isAfter(dayAgo);

        if (!isWithin24Hours) {
            return false;
        }

        long totalProvides = matchHistoryRepository.countTotalProvides(user.getId());
        boolean hasLessThan3Provides = totalProvides < 3;

        return hasLessThan3Provides;
    }

    /**
     * 컷오프 심사 결과 로그 출력
     */
    public boolean isEligibleTarget(User user, String requestedItemName, double cutoffScore) {
        // 1. 신규 유저 쉴드 판별 로그
        if (isNewUserWithShield(user)) {
            log.info("   ⭐ [심사 프리패스] 유저 #{} ({}) -> 신규 유저 쉴드 활성화 (점수 검사 우회)", user.getId(), user.getNickname());
            return true;
        }

        // 2. 가중치 계산 및 컷오프 비교 로그
        double score = calculateMatchingScore(user, requestedItemName);
        boolean eligible = score >= cutoffScore;

        // 🔥 [핵심 개선] 심사 통과/탈락 여부를 콘솔에 직관적으로 표시합니다.
        log.info("   ⚖️  [심사 최종결과] 유저 #{} ({}) -> 점수: {}점 (요구 컷오프: {}점) -> 📋 {}",
                user.getId(), user.getNickname(), score, cutoffScore, eligible ? "통과 (알림 발송 예정) 🎯" : "탈락 (점수 미달) ❌");

        return eligible;
    }
}