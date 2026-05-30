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
     * * @param user 대상 유저
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
     * 2. 최근 활동 점수 (30%) - 최근 접속 시간(last_active_at) 기준 
     * 방금 접속한 유저일수록 실시간 매칭 확률이 높으므로 높은 점수 부여
     */
    private double calculateRecentActivityScore(User user) {
        // 접속 기록이 없으면 0점 처리
        if (user.getLastActiveAt() == null) {
            return 0.0; 
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursElapsed = ChronoUnit.HOURS.between(user.getLastActiveAt(), now);

        // 1시간 이내 방금 전 접속한 유저는 만점 (100점)
        if (hoursElapsed <= 1) {
            return 100.0;
        }

        // 7일(168시간) 이상 장기 미접속 유저는 0점
        if (hoursElapsed >= 168) {
            return 0.0;
        }

        // 그 외의 경우 시간에 비례하여 선형 차감 (최대 168시간 기준)
        double decayRatio = 1.0 - ((double) hoursElapsed / 168.0);
        return Math.round(decayRatio * 1000.0) / 10.0; // 소수점 첫째 자리까지 반올림
    }

    /**
     * 3. 물건 대여 이력 (20%) 
     * - 동일/유사 물건 대여 이력: 100점 (만점)
     * - 다른 물건이라도 대여 이력 있음: 50점
     * - 대여 이력 없음: 0점
     */
    private double calculateItemHistoryScore(User user, String requestedItemName) {
        // 1. 요청 물건 이름 전처리 (공백 완전 제거 및 소문자 변환)
        // 예: "아이패드 충전기" -> "아이패드충전기"
        String cleanRequestedName = requestedItemName.replaceAll("\\s+", "").toLowerCase();

        // 2. 유사 물건 대여 이력 확인 (LIKE 검색 + 공백/대소문자 무시)
        boolean hasMatchingItem = matchHistoryRepository.hasProvidedSimilarItemBefore(user.getId(), cleanRequestedName);
        
        if (hasMatchingItem) {
            log.debug("User {} gets 100 points for matching item history.", user.getId());
            return 100.0;  // 100점 (최종 매칭 점수에 +20점 폭등)
        }

        // 3. 동일 물건은 아니지만 아무 물건이나 대여해준 '착한 유저'인지 확인
        // (isNewUserWithShield에서 사용하는 메서드 재활용)
        long totalProvides = matchHistoryRepository.countTotalProvides(user.getId());
        if (totalProvides > 0) {
            log.debug("User {} gets 50 points for general lending history.", user.getId());
            return 50.0;   // 50점 (최종 매칭 점수에 +10점 가산)
        }

        return 0.0; // 이력 아예 없음
    }

    /**
     * 신규 유저 쉴드 (Cold Start 방어) 판별
     * 조건: 공급 이력 < 3회 AND 가입 후 24시간 이내
     * * @param user 대상 유저
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
     * * @param user 대상 유저
     * @param requestedItemName 요청된 물건 이름
     * @param cutoffScore 컷오프 점수 (일반: 40점, 정예: 80점)
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