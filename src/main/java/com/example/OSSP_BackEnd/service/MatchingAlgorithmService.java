package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 매칭 알고리즘 서비스
 * Phase 1, 2, 3 단계별 타겟팅 및 FCM 발송 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchingAlgorithmService {

    private final UserRepository userRepository;
    private final MatchingScoreService matchingScoreService;
    private final FcmService fcmService;

    // 가중치 점수 컷오프
    private static final double GENERAL_TARGET_CUTOFF = 0.0;  // 일반 타겟: 40점 이상
    private static final double ELITE_TARGET_CUTOFF = 80.0;    // 정예 타겟: 80점 이상

    /**
     * 인접 건물 관계 정의 (실제 캠퍼스 지도 기반으로 수정 필요)
     * Key: 기준 건물, Value: 인접한 건물 리스트
     */
    private static final Map<String, List<String>> ADJACENT_BUILDINGS = new HashMap<>() {{
        put("INFO_CULTURE", Arrays.asList("HAKRIM", "WONHEUNG"));
        put("WONHEUNG", Arrays.asList("INFO_CULTURE", "MANHAE_PLAZA", "MAIN_BUILDING", "SHINGONG"));
        put("HAKRIM", Arrays.asList("INFO_CULTURE", "GEUMGANG"));
        put("GEUMGANG", Arrays.asList("HAKRIM", "MANHAE_PLAZA", "PALJEONGDO"));
        put("MAIN_BUILDING", Arrays.asList("WONHEUNG", "MANHAE_PLAZA", "PALJEONGDO", "CENTRAL_LIBRARY"));
        put("SHINGONG", Arrays.asList("WONHEUNG", "CENTRAL_LIBRARY"));
        put("CENTRAL_LIBRARY", Arrays.asList("SHINGONG", "MAIN_BUILDING", "MYUNGJIN", "SCIENCE"));
        put("MYUNGJIN", Arrays.asList("CENTRAL_LIBRARY", "PALJEONGDO", "LAW_SCHOOL", "SCIENCE"));
        put("SCIENCE", Arrays.asList("CENTRAL_LIBRARY", "MYUNGJIN", "MAIN_STADIUM"));
        put("MAIN_STADIUM", Arrays.asList("SCIENCE", "LAW_SCHOOL", "SCULPTURE"));
        put("SCULPTURE", Arrays.asList("MAIN_STADIUM", "HYEHWA", "SOCIAL_SCIENCE"));
        put("LAW_SCHOOL", Arrays.asList("PALJEONGDO", "MYUNGJIN", "MAIN_STADIUM", "HYEHWA"));
        put("HYEHWA", Arrays.asList("LAW_SCHOOL", "SCULPTURE", "SOCIAL_SCIENCE"));
        put("SOCIAL_SCIENCE", Arrays.asList("HYEHWA", "SCULPTURE", "CULTURE"));
        put("CULTURE", Arrays.asList("SOCIAL_SCIENCE"));
        put("PALJEONGDO", Arrays.asList("MAIN_BUILDING", "GEUMGANG", "LAW_SCHOOL", "MYUNGJIN"));
        put("MANHAE_PLAZA", Arrays.asList("GEUMGANG", "MAIN_BUILDING", "WONHEUNG"));
    }};

    /**
     * Phase 1: 대여 요청 즉시 실행되는 매칭
     * - 일반 타겟: 요청 건물 == 유저 건물 AND (가중치 >= 50 OR 신규 쉴드)
     * - 정예 타겟 (조기 편입): 인접 2차 구역 AND 가중치 >= 80
     * 
     * @param callRequest 대여 요청
     */
    @Async
    public void executePhase1Matching(CallRequest callRequest) {
        log.info("[PHASE 1] Starting matching for request #{}", callRequest.getId());
        
        String requestBuilding = callRequest.getBuildingName();
        String itemName = callRequest.getItemName();

        // 🔥 [이 로그를 추가해 주세요!] 프론트가 정확히 어떤 건물 이름을 보냈는지 확인
        log.info("요청 건물명(프론트가 보낸 값): {}", requestBuilding);

        // 1. 일반 타겟: 동일 건물에 있는 유저
        List<User> sameBuildingUsers = userRepository.findActiveUsersInBuilding(requestBuilding);
        // 🔥 [이 로그를 추가해 주세요!] DB에서 필터링 전에 몇 명을 꺼내왔는지 확인
        log.info("DB에서 꺼내온 동일 건물 유저 수: {}명", sameBuildingUsers.size());
        
        List<User> generalTargets = sameBuildingUsers.stream()
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, GENERAL_TARGET_CUTOFF))
                .collect(Collectors.toList());

        log.info("[PHASE 1] General Targets (same building): {} users", generalTargets.size());

        // 2. 정예 타겟 (조기 편입): 인접 2차 구역 건물 + 가중치 80점 이상
        List<String> adjacentBuildings = getAdjacentBuildings(requestBuilding, 1);  // 1차 인접
        List<User> adjacentUsers = userRepository.findActiveUsersInBuildings(adjacentBuildings);
        List<User> eliteTargets = adjacentUsers.stream()
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, ELITE_TARGET_CUTOFF))
                .collect(Collectors.toList());

        log.info("[PHASE 1] Elite Targets (adjacent + score >= 80): {} users", eliteTargets.size());

        // 3. FCM 알림 발송
        List<User> allPhase1Targets = new ArrayList<>();
        allPhase1Targets.addAll(generalTargets);
        allPhase1Targets.addAll(eliteTargets);

        sendNotifications(allPhase1Targets, callRequest, "Phase 1");
    }

    /**
     * Phase 2: Phase 1 발송 후 t분 뒤 수락자가 없을 경우 실행
     * - 일반 타겟: 인접 2차 구역 AND (가중치 >= 50 OR 신규 쉴드)
     * - 정예 타겟: 인접 3차 구역 AND 가중치 >= 80
     * 
     * @param callRequest 대여 요청
     */
    @Async
    public void executePhase2Matching(CallRequest callRequest) {
        log.info("[PHASE 2] Starting matching for request #{}", callRequest.getId());

        String requestBuilding = callRequest.getBuildingName();
        String itemName = callRequest.getItemName();

        // 1. 일반 타겟: 인접 1차 구역 (2차 확장)
        List<String> adjacentLevel1 = getAdjacentBuildings(requestBuilding, 1);
        List<User> level1Users = userRepository.findActiveUsersInBuildings(adjacentLevel1);
        List<User> generalTargets = level1Users.stream()
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, GENERAL_TARGET_CUTOFF))
                .collect(Collectors.toList());

        log.info("[PHASE 2] General Targets (adjacent level 1): {} users", generalTargets.size());

        // 2. 정예 타겟: 인접 2차 구역 (3차 확장) + 가중치 80점 이상
        List<String> adjacentLevel2 = getAdjacentBuildings(requestBuilding, 2);
        List<User> level2Users = userRepository.findActiveUsersInBuildings(adjacentLevel2);
        List<User> eliteTargets = level2Users.stream()
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, ELITE_TARGET_CUTOFF))
                .collect(Collectors.toList());

        log.info("[PHASE 2] Elite Targets (adjacent level 2 + score >= 80): {} users", eliteTargets.size());

        // 3. FCM 알림 발송
        List<User> allPhase2Targets = new ArrayList<>();
        allPhase2Targets.addAll(generalTargets);
        allPhase2Targets.addAll(eliteTargets);

        sendNotifications(allPhase2Targets, callRequest, "Phase 2");
    }

    /**
     * Phase 3: 최후 수단 - 수요자에게 보상금 인상 넛지 알림
     * 
     * @param callRequest 대여 요청
     */
    @Async
    public void executePhase3Fallback(CallRequest callRequest) {
        log.info("[PHASE 3] Fallback - sending nudge to requester for request #{}", callRequest.getId());

        User requester = callRequest.getRequester();
        
        if (requester.getFcmToken() != null) {
            String title = "매칭이 어려운 상황이에요 😢";
            String body = String.format(
                    "보상금을 올려서 다시 요청해보시겠어요? (현재: %d원)",
                    callRequest.getRewardAmt()
            );

            fcmService.sendMessageTo(requester.getFcmToken(), title, body);
            log.info("[PHASE 3] Nudge notification sent to requester #{}", requester.getId());
        } else {
            log.warn("[PHASE 3] Requester #{} has no FCM token", requester.getId());
        }
    }

    /**
     * 인접 건물 리스트 조회 (N차 확장 지원)
     * 
     * @param buildingName 기준 건물
     * @param level 확장 레벨 (1 = 1차 인접, 2 = 2차 인접...)
     * @return 인접 건물 리스트
     */
    private List<String> getAdjacentBuildings(String buildingName, int level) {
        if (level <= 0) {
            return Collections.emptyList();
        }

        Set<String> result = new HashSet<>();
        Set<String> currentLevel = new HashSet<>();
        currentLevel.add(buildingName);

        // BFS 방식으로 N차 인접 건물 탐색
        for (int i = 0; i < level; i++) {
            Set<String> nextLevel = new HashSet<>();
            for (String building : currentLevel) {
                List<String> adjacent = ADJACENT_BUILDINGS.getOrDefault(building, Collections.emptyList());
                nextLevel.addAll(adjacent);
            }
            result.addAll(nextLevel);
            currentLevel = nextLevel;
        }

        // 기준 건물 제외
        result.remove(buildingName);

        log.debug("Adjacent buildings for {} (level {}): {}", buildingName, level, result);
        return new ArrayList<>(result);
    }

    /**
     * FCM 알림 발송 (배치 처리)
     * 
     * @param targets 알림 받을 유저 리스트
     * @param callRequest 대여 요청 정보
     * @param phaseName 매칭 단계 이름
     */
    private void sendNotifications(List<User> targets, CallRequest callRequest, String phaseName) {
        if (targets.isEmpty()) {
            log.info("[{}] No targets to notify for request #{}", phaseName, callRequest.getId());
            return;
        }

        String title = String.format("🎯 새로운 대여 요청 (%s)", callRequest.getBuildingName());
        String body = String.format(
                "%s - 보상금 %d원 (기간: %d분)",
                callRequest.getItemName(),
                callRequest.getRewardAmt(),
                callRequest.getDuration()
        );

        int successCount = 0;
        for (User user : targets) {
            if (user.getFcmToken() != null) {
                try {
                    fcmService.sendMessageTo(user.getFcmToken(), title, body);
                    successCount++;
                } catch (Exception e) {
                    log.error("[{}] Failed to send notification to user #{}: {}",
                            phaseName, user.getId(), e.getMessage());
                }
            }
        }

        log.info("[{}] Notifications sent: {}/{} for request #{}",
                phaseName, successCount, targets.size(), callRequest.getId());
    }
}
