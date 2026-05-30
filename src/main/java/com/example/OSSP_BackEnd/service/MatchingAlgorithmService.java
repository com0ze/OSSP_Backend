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
 * 매칭 알고리즘 서비스 (디버깅 특화 버전)
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
    private static final double GENERAL_TARGET_CUTOFF = 0.0;  // 디버깅을 위해 0점 처리 중
    private static final double ELITE_TARGET_CUTOFF = 80.0;    // 정예 타겟: 80점 이상

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

    @Async
    public void executePhase1Matching(CallRequest callRequest) {
        log.info("================== [PHASE 1 매칭 시작] ==================");
        log.info("대여 요청 ID: #{}", callRequest.getId());
        
        String requestBuilding = callRequest.getBuildingName();
        String itemName = callRequest.getItemName();
        Long requesterId = callRequest.getRequester().getId();

        log.info("▶ [STEP 1] 프론트엔드 입력값 검증");
        log.info(" - 요청자 ID: {}", requesterId);
        log.info(" - 요청 건물명 (String 텍스트 정확도 확인): [{}]", requestBuilding);
        log.info(" - 요청 아이템명: [{}]", itemName);

        // 1. 일반 타겟: 동일 건물에 있는 유저
        List<User> sameBuildingUsers = userRepository.findActiveUsersInBuilding(requestBuilding);
        
        log.info("▶ [STEP 2] DB 조회 1차 결과 ( userRepository.findActiveUsersInBuilding )");
        log.info(" - DB에서 꺼내온 동일 건물 유저 수: {}명", sameBuildingUsers.size());
        
        // DB에서 가져온 유저들의 실제 상세 스펙 출력
        if (!sameBuildingUsers.isEmpty()) {
            for (User u : sameBuildingUsers) {
                log.info("   -> [DB 추출 유저] ID: {}, 닉네임: {}, 건물: [{}], OnDuty상태: {}", 
                        u.getId(), u.getNickname(), u.getCurrentBuilding(), u.getIsOnDuty());
            }
        } else {
            log.warn("🚨 [경고] DB에서 유저를 한 명도 찾지 못했습니다! (건물명 불일치 또는 is_on_duty 매핑 에러 의심)");
        }

        log.info("▶ [STEP 3] 컷오프( {}점 ) 필터링 심사 시작", GENERAL_TARGET_CUTOFF);
        List<User> generalTargets = sameBuildingUsers.stream()
                // 본인(요청자) 제외 로직 (혹시라도 DB에서 본인을 가져왔을 경우 대비)
                .filter(user -> !user.getId().equals(requesterId))
                .peek(user -> log.info(" - 심사 중... 대상 유저ID: {}", user.getId()))
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, GENERAL_TARGET_CUTOFF))
                .peek(user -> log.info("   ★ [일반 타겟 합격!] 유저ID: {}", user.getId()))
                .collect(Collectors.toList());

        log.info("▶ [STEP 4] Phase 1 일반 타겟 최종 인원: {}명", generalTargets.size());

        // 2. 정예 타겟 (조기 편입)
        List<String> adjacentBuildings = getAdjacentBuildings(requestBuilding, 1);
        List<User> adjacentUsers = userRepository.findActiveUsersInBuildings(adjacentBuildings);
        
        List<User> eliteTargets = adjacentUsers.stream()
                .filter(user -> !user.getId().equals(requesterId))
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, ELITE_TARGET_CUTOFF))
                .collect(Collectors.toList());

        log.info("▶ [STEP 5] Phase 1 정예 타겟 (80점 이상) 최종 인원: {}명", eliteTargets.size());
        log.info("=========================================================");

        // 3. FCM 알림 발송
        List<User> allPhase1Targets = new ArrayList<>();
        allPhase1Targets.addAll(generalTargets);
        allPhase1Targets.addAll(eliteTargets);

        sendNotifications(allPhase1Targets, callRequest, "Phase 1");
    }

    @Async
    public void executePhase2Matching(CallRequest callRequest) {
        log.info("================== [PHASE 2 매칭 시작] ==================");
        String requestBuilding = callRequest.getBuildingName();
        String itemName = callRequest.getItemName();
        Long requesterId = callRequest.getRequester().getId();

        List<String> adjacentLevel1 = getAdjacentBuildings(requestBuilding, 1);
        List<User> level1Users = userRepository.findActiveUsersInBuildings(adjacentLevel1);
        
        List<User> generalTargets = level1Users.stream()
                .filter(user -> !user.getId().equals(requesterId))
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, GENERAL_TARGET_CUTOFF))
                .collect(Collectors.toList());

        List<String> adjacentLevel2 = getAdjacentBuildings(requestBuilding, 2);
        List<User> level2Users = userRepository.findActiveUsersInBuildings(adjacentLevel2);
        
        List<User> eliteTargets = level2Users.stream()
                .filter(user -> !user.getId().equals(requesterId))
                .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, ELITE_TARGET_CUTOFF))
                .collect(Collectors.toList());

        log.info("[PHASE 2] General Targets: {} 명, Elite Targets: {} 명", generalTargets.size(), eliteTargets.size());
        log.info("=========================================================");

        List<User> allPhase2Targets = new ArrayList<>();
        allPhase2Targets.addAll(generalTargets);
        allPhase2Targets.addAll(eliteTargets);

        sendNotifications(allPhase2Targets, callRequest, "Phase 2");
    }

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

    private List<String> getAdjacentBuildings(String buildingName, int level) {
        if (level <= 0) {
            return Collections.emptyList();
        }

        Set<String> result = new HashSet<>();
        Set<String> currentLevel = new HashSet<>();
        currentLevel.add(buildingName);

        for (int i = 0; i < level; i++) {
            Set<String> nextLevel = new HashSet<>();
            for (String building : currentLevel) {
                List<String> adjacent = ADJACENT_BUILDINGS.getOrDefault(building, Collections.emptyList());
                nextLevel.addAll(adjacent);
            }
            result.addAll(nextLevel);
            currentLevel = nextLevel;
        }

        result.remove(buildingName);
        return new ArrayList<>(result);
    }

    private void sendNotifications(List<User> targets, CallRequest callRequest, String phaseName) {
        if (targets.isEmpty()) {
            log.warn("🚨 [{}] 타겟 유저가 0명이므로 알림 발송을 취소합니다. (요청 ID: #{})", phaseName, callRequest.getId());
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
                    log.info(">> [{}] 유저 #{} ({}) 에게 FCM 전송 시도 중...", phaseName, user.getId(), user.getNickname());
                    fcmService.sendMessageTo(user.getFcmToken(), title, body);
                    successCount++;
                } catch (Exception e) {
                    log.error("[{}] 유저 #{} FCM 발송 실패: {}", phaseName, user.getId(), e.getMessage());
                }
            } else {
                log.warn("[{}] 유저 #{} 은(는) FCM 토큰이 NULL 입니다. 발송 스킵.", phaseName, user.getId());
            }
        }

        log.info("✅ [{}] 최종 알림 전송 완료: 성공 {}/{} 건 (요청 ID: #{})", 
                phaseName, successCount, targets.size(), callRequest.getId());
    }
}