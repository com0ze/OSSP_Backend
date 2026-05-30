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
 * - 1분마다 동적 확장 매칭 실행
 * - 잠수 유저 차단 (매일 새벽 4시)
 * - 오래된 요청 자동 취소
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingScheduler {

    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;
    private final MatchingAlgorithmService matchingAlgorithmService;

    /**
     * 🔥 [핵심] BFS 기반 동적 확장 매칭 스케줄러
     * 매 1분마다 실행하여 WAITING 상태 요청들의 경과 시간을 계산하고,
     * 해당 경과 시간(분)을 거리(Distance)로 사용하여 BFS 탐색을 수행합니다.
     */
    @Scheduled(cron = "0 * * * * *")  // 매 분 0초에 실행
    @Transactional
    public void executeDynamicMatching() {
        LocalDateTime now = LocalDateTime.now();

        // WAITING 상태인 모든 요청 조회
        List<CallRequest> waitingRequests = callRequestRepository
                .findByStatusWithRequesterOrderByCreatedAtDesc(RequestStatus.WAITING);

        if (waitingRequests.isEmpty()) {
            log.debug("[SCHEDULER] 현재 처리 중인 대여 요청이 없습니다.");
            return;
        }

        log.info("========== [매칭 스케줄러] 실행 시각: {} ==========", now);
        log.info("▶ 처리 대상 요청 건수: {}건", waitingRequests.size());

        for (CallRequest request : waitingRequests) {
            // 요청 생성 시간으로부터 경과된 분(Minute) 계산
            long elapsedMinutes = ChronoUnit.MINUTES.between(request.getCreatedAt(), now);
            
            log.info("→ 요청 ID: #{}, 경과 시간: {}분", request.getId(), elapsedMinutes);

            // 동적 확장 매칭 알고리즘 호출 (경과 시간 = BFS 거리)
            matchingAlgorithmService.executeDynamicMatching(request, (int) elapsedMinutes);
        }

        log.info("==================================================");
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
