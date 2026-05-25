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
            throw new InvalidRequestStateException("현재 요청을 수락할 수 없습니다. (상태: " + callRequest.getStatus() +