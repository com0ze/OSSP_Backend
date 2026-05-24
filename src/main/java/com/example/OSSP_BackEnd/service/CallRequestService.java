package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.exception.InvalidRequestStateException;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.exception.SelfAcceptNotAllowedException;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // [최적화] 조회 메서드가 많으므로 기본 읽기 전용 모드 활성화
public class CallRequestService {

    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;
    private final MatchHistoryRepository matchHistoryRepository;

    /*
     대여 요청 생성 (수요자)
     */
    @Transactional
    public CallRequest createRequest(RequestCreateDto dto, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("요청자를 찾을 수 없습니다."));

        CallRequest callRequest = CallRequest.builder()
                .itemName(dto.itemName())
                .buildingName(dto.buildingName())
                .rewardAmt(dto.rewardAmt())
                .duration(dto.duration())
                .memo(dto.memo())
                .requester(requester)
                .status(RequestStatus.WAITING)
                .build();

        return callRequestRepository.save(callRequest);
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

    /*
     내 대여 현황 조회 (type 파라미터로 진행 중/과거 내역 구분)
     */
    public List<CallRequest> getMyRequests(Long userId, String type) {
        List<RequestStatus> statuses;
        
        if ("history".equalsIgnoreCase(type)) {
            // 과거 내역: COMPLETED, CANCELED
            statuses = List.of(RequestStatus.COMPLETED, RequestStatus.CANCELED);
        } else {
            // 진행 중 내역 (기본값): WAITING, MATCHED, IN_USE
            statuses = List.of(RequestStatus.WAITING, RequestStatus.MATCHED, RequestStatus.IN_USE);
        }
        
        return callRequestRepository.findByRequesterIdAndStatusInOrderByCreatedAtDesc(userId, statuses);
    }
}