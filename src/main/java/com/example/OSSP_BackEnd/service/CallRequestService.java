package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.exception.DuplicateWaitingRequestException;
import com.example.OSSP_BackEnd.exception.InvalidRequestStateException;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.exception.SelfAcceptNotAllowedException;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization; // 💡 추가됨
import org.springframework.transaction.support.TransactionSynchronizationManager; // 💡 추가됨

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // [최적화] 조회 메서드가 많으므로 기본 읽기 전용 모드 활성화
@Slf4j
public class CallRequestService {

    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final MatchingAlgorithmService matchingAlgorithmService;

    /*
     대여 요청 생성 (수요자)
     */
    @Transactional
    public CallRequest createRequest(RequestCreateDto dto, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("요청자를 찾을 수 없습니다."));

        // 💡 1. 도배 방지: 단순 문자열 대신 Enum 객체를 정확히 넘겨주어 500 에러를 방지합니다.
        if (callRequestRepository.hasWaitingRequest(userId, RequestStatus.WAITING)) {
            log.warn("User #{} attempted to create duplicate waiting request", userId);
            throw new DuplicateWaitingRequestException();
        }

        CallRequest callRequest = CallRequest.builder()
                .itemName(dto.itemName())
                .buildingName(dto.buildingName())
                .rewardAmt(dto.rewardAmt())
                .duration(dto.duration())
                .memo(dto.memo())
                .requester(requester)
                .status(RequestStatus.WAITING)
                .build();

        CallRequest savedRequest = callRequestRepository.save(callRequest);

        // 💡 2. 동시성 이슈 방어: DB 트랜잭션이 완벽히 커밋(Commit)된 직후에 비동기 매칭을 실행하도록 예약합니다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Triggering Phase 1 matching for request #{} after DB commit", savedRequest.getId());
                matchingAlgorithmService.executePhase1Matching(savedRequest);
            }
        });

        return savedRequest;
    }

    /*
     대여 요청 전체/주변 대기열 목록 조회 (상태 필터링 반영)
     */
    public List<CallRequest> getRequests(RequestStatus status) {
        if (status != null) {
            // 패치조인(JOIN FETCH) 쿼리 호출
            return callRequestRepository.findByStatusWithRequesterOrderByCreatedAtDesc(status);
        } else {
            return callRequestRepository.findAllWithRequesterOrderByCreatedAtDesc();
        }
    }

    /**
     * 3. 대여 요청 단건 상세 조회
     */
    public CallRequest getRequestDetail(Long requestId) {
        return callRequestRepository.findByIdWithRequester(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));
    }

    /*
      대여 요청 수락 (공급자 매칭 성사)
     */
    @Transactional
    public MatchHistory acceptRequest(Long requestId, RequestAcceptRequestDto dto) {
        CallRequest callRequest = callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));

        // [방어 로직 1] WAITING 상태인 건만 수락 가능
        if (callRequest.getStatus() != RequestStatus.WAITING) {
            throw new InvalidRequestStateException("현재 요청을 수락할 수 없습니다. (상태: " + callRequest.getStatus() + ")");
        }

        User provider = userRepository.findById(dto.providerId())
                .orElseThrow(() -> new ResourceNotFoundException("제공자를 찾을 수 없습니다."));

        // [방어 로직 2] 본인이 호출한 글을 본인이 수락하는 행위 차단
        if (callRequest.getRequester().getId().equals(provider.getId())) {
            throw new SelfAcceptNotAllowedException("자신의 요청을 수락할 수 없습니다.");
        }

        // 객체지향 상태 전이 (WAITING -> MATCHED)
        callRequest.markAsMatched();

        MatchHistory matchHistory = MatchHistory.create(callRequest, provider);
        return matchHistoryRepository.save(matchHistory);
    }

    /*
     대여 요청 취소
     */
    @Transactional
    public CallRequest cancelRequest(Long requestId) {
        CallRequest callRequest = callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));

        // 유연한 정책 반영: 대기중(WAITING)이거나 매칭된 상태(MATCHED)에서 모두 취소 가능
        if (callRequest.getStatus() != RequestStatus.WAITING && callRequest.getStatus() != RequestStatus.MATCHED) {
            throw new InvalidRequestStateException("현재 상태(" + callRequest.getStatus() + ")에서는 요청을 취소할 수 없습니다.");
        }

        callRequest.markAsCanceled();
        return callRequestRepository.save(callRequest);
    }

    /*
     물품 전달 완료 처리 (MATCHED -> IN_USE)
     */
    @Transactional
    public CallRequest handoverItem(Long requestId) {
        CallRequest callRequest = callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));

        if (callRequest.getStatus() != RequestStatus.MATCHED) {
            throw new InvalidRequestStateException("현재 상태(" + callRequest.getStatus() + ")에서는 물품을 전달할 수 없습니다. 매칭된 상태여야 합니다.");
        }

        callRequest.markAsInUse();
        return callRequestRepository.save(callRequest);
    }

    /*
     반납 완료 및 최종 거래 종료 처리 (IN_USE -> COMPLETED)
     */
    @Transactional
    public CallRequest completeRequest(Long requestId) {
        CallRequest callRequest = callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));

        if (callRequest.getStatus() != RequestStatus.IN_USE) {
            throw new InvalidRequestStateException("현재 상태(" + callRequest.getStatus() + ")에서는 거래를 완료할 수 없습니다. 대여중인 상태여야 합니다.");
        }

        callRequest.markAsCompleted();

        MatchHistory matchHistory = matchHistoryRepository.findByRequestIdWithRequest(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("매칭 기록을 찾을 수 없습니다."));
        
        // 반납 시간 기록 및 매칭 종료
        matchHistory.markAsReturned(LocalDateTime.now());
        matchHistoryRepository.save(matchHistory);

        return callRequestRepository.save(callRequest);
    }

    /**
     * 내 대여 요청 목록을 상태별로 조회합니다.
     * @param userId 현재 사용자 ID
     * @param status 조회할 특정 상태 (null일 경우 모든 상태 조회)
     * @return 상태에 맞는 대여 요청 목록
     */
    public List<CallRequest> getMyRequestsByStatus(Long userId, RequestStatus status) {
        if (status != null) {
            // 특정 상태가 지정된 경우, 해당 상태의 요청만 조회
            return callRequestRepository.findByRequesterIdAndStatusInOrderByCreatedAtDesc(userId, List.of(status));
        } else {
            // 상태가 지정되지 않은 경우, 모든 상태의 요청 조회
            return callRequestRepository.findByRequesterIdAndStatusInOrderByCreatedAtDesc(userId, Arrays.asList(RequestStatus.values()));
        }
    }
    public List<CallRequest> getMyAndAcceptedRequestsByStatus(Long userId, RequestStatus status) {
        // 내가 생성한 요청 목록 조회
        List<CallRequest> myRequests = getMyRequestsByStatus(userId, status);

        // 내가 수락한 요청 목록 조회
        List<CallRequest> acceptedRequests;
        if (status != null) {
            acceptedRequests = matchHistoryRepository.findCallRequestsByProviderIdAndStatus(userId, status);
        } else {
            acceptedRequests = matchHistoryRepository.findCallRequestsByProviderId(userId);
        }

        // 두 목록을 합치고 중복을 제거한 후, 생성 시간 기준으로 내림차순 정렬
        return Stream.concat(myRequests.stream(), acceptedRequests.stream())
                .distinct()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .collect(Collectors.toList());
    }
}