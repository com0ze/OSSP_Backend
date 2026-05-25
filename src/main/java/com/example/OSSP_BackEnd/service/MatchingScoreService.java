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
 * 매칭 알고리즘 가중치 계산 서비스
 * 0~100점 만점의 matching_power_score를 계산
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchingScoreService {

    private final MatchHistoryRepository matchHistoryRepository;

    // 가중치 비율
    private static final double MANNER_SCORE_WEIGHT = 0.5;  // 50%
    private static final double RECENT_ACTIVITY_WEIGHT = 0.3;  // 30%
    private static final double ITEM_HISTORY_WEIGHT = 0.2;  // 20%

    /**
     * 매칭 파워 스코어 계산 (0~100점)
     * 
     * @param user 대상 유저
     * @param requestedItemName 수요자가 요청한 물건 이름
     * @return 매칭 파워 스코어 (0~100)
     */
    public double calculateMatchingScore(User user, String requestedItemName) {
        double mannerScore = calculateMannerScore(user);
        double recentActivityScore = calculateRecentActivityScore(user);
        double itemHistoryScore = calculateItemHistoryScore(user, requestedItemName);

        double totalScore = (mannerScore * MANNER_SCORE_WEIGHT)
                + (recentActivityScore * RECENT_ACTIVITY_WEIGHT)
                + (itemHistoryScore * ITEM_HISTORY_WEIGHT);

        log.debug("User {} Matching Score: Total={}, Manner={}, Recent={}, Item={}",
                user.getId(), totalScore, mannerScore, recentActivityScore, itemHistoryScore);

        return Math.round(totalScore * 100.0) / 100.0;  // 소수점 둘째 자리까지
    }

    /**
     * 1. 매너 점수 (50%) - 5.0 만점 기준을 100점으로 환산
     * 예: 4.5점 -> 90점
     */
    private double calculateMannerScore(User user) {
        if (user.getMannerScore() == null) {
            return 60.0;  // 기본값 (3.0점 기준)
        }

        // 5.0 만점을 100점 만점으로 환산
        return user.getMannerScore()
                .divide(BigDecimal.valueOf(5.0), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100.0))
                .doubleValue();
    }

    /**
     * 2. 최근 7일 대여 횟수 (30%) - 비례 계산
     * 0회 = 0점, 5회 이상 = 만점(100점)
     */
    private double calculateRecentActivityScore(User user) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        long recentCount = matchHistoryRepository.countRecentCompletedProvides(user.getId(), sevenDaysAgo);

        // 5회 이상이면 만점
        if (recentCount >= 5) {
            return 100.0;
        }

        // 비례 계산: (실제 횟수 / 5) * 100
        return (recentCount / 5.0) * 100.0;
    }

    /**
     * 3. 물건 대여 이력 (20%) - 해당 물건을 빌려준 적이 있으면 20점, 없으면 0점
     */
    private double calculateItemHistoryScore(User user, String requestedItemName) {
        boolean hasHistory = matchHistoryRepository.hasProvidedItem(user.getId(), requestedItemName);
        return hasHistory ? 100.0 : 0.0;  // 100점 or 0점 (가중치 20%가 곱해짐)
    }

    /**
     * 신규 유저 쉴드 (Cold Start 방어) 판별
     * 조건: 공급 이력 < 3회 AND 가입 후 24시간 이내
     * 
     * @param user 대상 유저
     * @return 신규 유저 쉴드 적용 대상이면 true
     */
    public boolean isNewUserWithShield(User user) {
        // 1. 가입 후 24시간 이내 체크
        LocalDateTime dayAgo = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        boolean isWithin24Hours = user.getCreatedAt().isAfter(dayAgo);

        if (!isWithin24Hours) {
            return false;
        }

        // 2. 공급 이력 3회 미만 체크
        long totalProvides = matchHistoryRepository.countTotalProvides(user.getId());
        boolean hasLessThan3Provides = totalProvides < 3;

        log.debug("User {} New User Shield Check: within24h={}, provides={}, eligible={}",
                user.getId(), isWithin24Hours, totalProvides, hasLessThan3Provides);

        return hasLessThan3Provides;
    }

    /**
     * 매칭 타겟 판별 (가중치 + 신규 유저 쉴드 종합 판단)
     * 
     * @param user 대상 유저
     * @param requestedItemName 요청된 물건 이름
     * @param cutoffScore 컷오프 점수 (일반: 50점, 정예: 80점)
     * @return 타겟 조건 충족 시 true
     */
    public boolean isEligibleTarget(User user, String requestedItemName, double cutoffScore) {
        // 신규 유저 쉴드가 적용되면 점수 무관하게 1차 타겟 자격 부여
        if (isNewUserWithShield(user)) {
            log.info("User {} eligible by NEW USER SHIELD (score check bypassed)", user.getId());
            return true;
        }

        // 일반 유저는 가중치 점수 기준으로 판별
        double score = calculateMatchingScore(user, requestedItemName);
        boolean eligible = score >= cutoffScore;

        log.debug("User {} eligibility: score={}, cutoff={}, eligible={}",
                user.getId(), score, cutoffScore, eligible);

        return eligible;
    }
}
