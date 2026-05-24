package com.example.OSSP_BackEnd.scheduler;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import com.example.OSSP_BackEnd.service.MatchingAlgorithmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 매칭 알고리즘 스케줄러
 * - Phase 2/3 단계별 매칭 실행
 * - 잠수 유저 차단 (매일 새벽 4시)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingScheduler {

    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;
    private final MatchingAlgorithmService matchingAlgorithmService;

    // Phase 2 실행 대기 시간 (분) - Phase 1 발송 후 5분 뒤
    private static final int PHASE2_DELAY_MINUTES = 5;

    // Phase 3 실행 대기 시간 (분) - Phase 2 발송 후 10분 뒤
    private static final int PHASE3_DELAY_MINUTES = 15;  // Phase 1 기준 총 15분

    /**
     * Phase 2 & 3 매칭 스케줄러
     * 1분마다 실행하여 WAITING 상태 요청들을 확인하고 단계별 매칭 실행
     */
    @Scheduled(cron = "0 * * * * *")  // 매 분 0초에 실행
    @Transactional
    public void executePhase2And3Matching() {
        LocalDateTime now = LocalDateTime.now();

        // WAITING 상태인 모든 요청 조회
        List<CallRequest> waitingRequests = callRequestRepository
                .findByStatusWithRequesterOrderByCreatedAtDesc(RequestStatus.WAITING);

        for (CallRequest request : waitingRequests) {
            long minutesSinceCreated = ChronoUnit.MINUTES.between(request.getCreatedAt(), now);

            // Phase 2 실행 시점 체크 (5분 경과)
            if (minutesSinceCreated >= PHASE2_DELAY_MINUTES 
                    && minutesSinceCreated < PHASE2_DELAY_MINUTES + 1) {
                log.info("[SCHEDULER] Triggering Phase 2 for request #{} ({}min elapsed)",
                        request.getId(), minutesSinceCreated);
                matchingAlgorithmService.executePhase2Matching(request);
            }

            // Phase 3 실행 시점 체크 (15분 경과)
            if (minutesSinceCreated >= PHASE3_DELAY_MINUTES 
                    && minutesSinceCreated < PHASE3_DELAY_MINUTES + 1) {
                log.info("[SCHEDULER] Triggering Phase 3 fallback for request #{} ({}min elapsed)",
                        request.getId(), minutesSinceCreated);
                matchingAlgorithmService.executePhase3Fallback(request);
            }
        }
    }

    /**
     * 잠수 유저 차단 스케줄러
     * 매일 새벽 4시에 실행하여 마지막 활동 시간이 5일 이전인 유저들의 알림 받기를 OFF로 변경
     */
    @Scheduled(cron = "0 0 4 * * *")  // 매일 오전 4시 0분 0초
    @Transactional
    public void deactivateInactiveUsers() {
        log.info("[SCHEDULER] Starting inactive user deactivation job");

        LocalDateTime fiveDaysAgo = LocalDateTime.now().minus(5, ChronoUnit.DAYS);

        // 5일간 활동 없는 유저 조회
        List<User> inactiveUsers = userRepository.findInactiveUsers(fiveDaysAgo);

        if (inactiveUsers.isEmpty()) {
            log.info("[SCHEDULER] No inactive users found");
            return;
        }

        // 알림 받기 상태 OFF로 변경
        int deactivatedCount = 0;
        for (User user : inactiveUsers) {
            user.updateDutyStatus(false);
            deactivatedCount++;
            log.debug("[SCHEDULER] Deactivated user #{} (last active: {})",
                    user.getId(), user.getLastActiveAt());
        }

        userRepository.saveAll(inactiveUsers);

        log.info("[SCHEDULER] Inactive user deactivation completed: {} users deactivated",
                deactivatedCount);
    }

    /**
     * 오래된 WAITING 요청 자동 취소 (선택적 기능)
     * 24시간 이상 WAITING 상태로 남아있는 요청을 자동 취소
     */
    @Scheduled(cron = "0 30 * * * *")  // 매 시간 30분에 실행
    @Transactional
    public void cancelExpiredWaitingRequests() {
        log.info("[SCHEDULER] Starting expired waiting requests cleanup");

        LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

        List<CallRequest> waitingRequests = callRequestRepository
                .findByStatusWithRequesterOrderByCreatedAtDesc(RequestStatus.WAITING);

        int canceledCount = 0;
        for (CallRequest request : waitingRequests) {
            if (request.getCreatedAt().isBefore(yesterday)) {
                request.markAsCanceled();
                canceledCount++;
                log.debug("[SCHEDULER] Auto-canceled expired request #{} (created: {})",
                        request.getId(), request.getCreatedAt());
            }
        }

        if (canceledCount > 0) {
            callRequestRepository.saveAll(waitingRequests);
            log.info("[SCHEDULER] Expired requests cleanup completed: {} requests canceled",
                    canceledCount);
        } else {
            log.info("[SCHEDULER] No expired requests found");
        }
    }
}
