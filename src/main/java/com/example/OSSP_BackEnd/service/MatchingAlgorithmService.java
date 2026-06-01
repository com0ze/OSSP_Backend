package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 동적 확장 매칭 알고리즘 서비스
 * 시간에 따라 파도(Wave)처럼 건물을 탐색하며, 더 이상 탐색할 건물이 없으면 자동 종료
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchingAlgorithmService {

    private final UserRepository userRepository;
    private final CallRequestRepository callRequestRepository; // 💡 상태 업데이트를 위한 저장소 추가
    private final MatchingScoreService matchingScoreService;
    private final FcmService fcmService;

    // 가중치 점수 컷오프
    private static final double GENERAL_TARGET_CUTOFF = 30.0;  // 디버깅/테스트 후 40.0으로 복구
    private static final double ELITE_TARGET_CUTOFF = 70.0;

    // 캠퍼스 건물 인접 리스트 (그래프 노드)
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
     * 1분 단위로 호출되는 동적 확장 매칭 메서드
     * @param callRequest 대여 요청 객체
     * @param elapsedMinutes 경과 시간 (0분, 1분, 2분...)
     */
    @Async
    @Transactional // 💡 DB에 변경된 상태(CANCELED)를 커밋하기 위해 Write 권한 트랜잭션 추가
    public void executeDynamicMatching(CallRequest callRequest, int elapsedMinutes) {
        log.info("========== [동적 매칭 확장] 요청 ID: #{}, 경과 시간: {}분 ==========", callRequest.getId(), elapsedMinutes);

        String requestBuilding = callRequest.getBuildingName();
        String itemName = callRequest.getItemName();
        Long requesterId = callRequest.getRequester().getId();

        // [핵심 로직] 경과 시간에 따라 탐색할 건물 거리를 정확히 계산합니다.
        Set<String> generalTargetBuildings = getBuildingsAtExactDistance(requestBuilding, elapsedMinutes);
        Set<String> eliteTargetBuildings = getBuildingsAtExactDistance(requestBuilding, elapsedMinutes + 1);

        log.info("▶ 거리 {} (일반 타겟 건물): {}", elapsedMinutes, generalTargetBuildings);
        log.info("▶ 거리 {} (정예 타겟 건물): {}", elapsedMinutes + 1, eliteTargetBuildings);

        // 🚨 종료 조건: 일반/정예 타겟 건물이 더 이상 존재하지 않음 = 캠퍼스 전체 탐색 끝
        if (generalTargetBuildings.isEmpty() && eliteTargetBuildings.isEmpty()) {
            log.warn("🚨 [탐색 종료] 캠퍼스 내 모든 건물을 탐색했습니다. 매칭 실패 넛지 알림을 발송합니다.");
            executeFallbackNotification(callRequest);
            return;
        }

        List<User> allTargetsToNotify = new ArrayList<>();

        // 1. 일반 타겟 필터링
        if (!generalTargetBuildings.isEmpty()) {
            List<User> generalUsers = userRepository.findActiveUsersInBuildings(new ArrayList<>(generalTargetBuildings));
            List<User> filteredGeneral = generalUsers.stream()
                    .filter(user -> !user.getId().equals(requesterId))
                    .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, GENERAL_TARGET_CUTOFF))
                    .collect(Collectors.toList());
            allTargetsToNotify.addAll(filteredGeneral);
            log.info(" - 일반 타겟 합격자: {}명", filteredGeneral.size());
        }

        // 2. 정예 타겟 필터링
        if (!eliteTargetBuildings.isEmpty()) {
            List<User> eliteUsers = userRepository.findActiveUsersInBuildings(new ArrayList<>(eliteTargetBuildings));
            List<User> filteredElite = eliteUsers.stream()
                    .filter(user -> !user.getId().equals(requesterId))
                    .filter(user -> matchingScoreService.isEligibleTarget(user, itemName, ELITE_TARGET_CUTOFF))
                    .collect(Collectors.toList());
            allTargetsToNotify.addAll(filteredElite);
            log.info(" - 정예 타겟 합격자: {}명", filteredElite.size());
        }

        // 3. 중복 제거 및 최종 발송
        List<User> uniqueTargets = allTargetsToNotify.stream()
                .distinct()
                .collect(Collectors.toList());

        sendNotifications(uniqueTargets, callRequest, elapsedMinutes + "분차 확장");
        log.info("=========================================================");
    }

    /**
     * 캠퍼스 모든 건물 탐색 완료 시 수요자에게 쏘는 매칭 실패 알림 및 스케줄러 타겟 제외 로직
     */
    private void executeFallbackNotification(CallRequest callRequest) {
        User requester = callRequest.getRequester();
        if (requester.getFcmToken() != null) {
            String title = "매칭이 어려운 상황이에요 😢";
            String body = String.format("보상금을 올려서 다시 요청해보시겠어요? (현재: %d원)", callRequest.getRewardAmt());

            // 데이터 페이로드 생성
            Map<String, String> data = new HashMap<>();
            data.put("type", "MATCHING_FAILED");
            data.put("requestId", callRequest.getId().toString());
            data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

            fcmService.sendMessageTo(requester.getFcmToken(), title, body, data);
            log.info("✅ 매칭 실패 넛지 알림 전송 완료 (요청 ID: #{})", callRequest.getId());
        }

        // 🔥 [좀비 스케줄러 방지 로직] 상태를 CANCELED로 업데이트하고 DB에 즉시 저장
        callRequest.markAsCanceled(); // (혹시 메서드명이 다르면 callRequest.setStatus(RequestStatus.CANCELED); 로 변경해주세요)
        callRequestRepository.save(callRequest);

        log.info("🛑 대여 요청 #{} 상태를 CANCELED로 변경하여 스케줄러 무한 루프를 차단했습니다.", callRequest.getId());
    }

    /**
     * BFS 알고리즘을 사용하여 '정확히 N번째 거리'에 있는 건물들만 추출합니다.
     */
    private Set<String> getBuildingsAtExactDistance(String startBuilding, int targetDistance) {
        if (targetDistance == 0) {
            return new HashSet<>(Collections.singletonList(startBuilding));
        }

        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.add(startBuilding);
        visited.add(startBuilding);

        int currentDistance = 0;

        while (!queue.isEmpty()) {
            if (currentDistance == targetDistance) {
                return new HashSet<>(queue);
            }

            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                List<String> neighbors = ADJACENT_BUILDINGS.getOrDefault(current, Collections.emptyList());

                for (String neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            currentDistance++;
        }

        return Collections.emptySet();
    }

    private void sendNotifications(List<User> targets, CallRequest callRequest, String logPrefix) {
        if (targets.isEmpty()) {
            log.warn("🚨 [{}] 조건에 맞는 타겟 유저가 0명입니다.", logPrefix);
            return;
        }

        String title = String.format("🎯 새로운 대여 요청 (%s)", callRequest.getBuildingName());
        String body = String.format("%s - 보상금 %d원 (기간: %d시간)",
                callRequest.getItemName(), callRequest.getRewardAmt(), callRequest.getDuration()/60);

        // 데이터 페이로드 생성
        Map<String, String> data = new HashMap<>();
        data.put("type", "RENTAL_REQUEST");
        data.put("requestId", callRequest.getId().toString());
        data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

        int successCount = 0;
        for (User user : targets) {
            if (user.getFcmToken() != null) {
                try {
                    fcmService.sendMessageTo(user.getFcmToken(), title, body, data);
                    successCount++;
                } catch (Exception e) {
                    log.error("[{}] 유저 #{} FCM 발송 실패: {}", logPrefix, user.getId(), e.getMessage());
                }
            }
        }
        log.info("✅ [{}] 알림 전송 성공: {}/{} 건", logPrefix, successCount, targets.size());
    }
}