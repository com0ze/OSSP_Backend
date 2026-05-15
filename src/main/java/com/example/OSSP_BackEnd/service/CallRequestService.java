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
public class CallRequestService {

    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;
    private final MatchHistoryRepository matchHistoryRepository;

    @Transactional
    public CallRequest createRequest(RequestCreateDto dto) {
        User requester = userRepository.findById(dto.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException("요청자를 찾을 수 없습니다."));

        CallRequest callRequest = CallRequest.create(
                dto.itemName(),
                dto.buildingName(),
                dto.rewardAmt(),
                dto.duration(),
                dto.memo(),
                requester
        );
        return callRequestRepository.save(callRequest);
    }

    @Transactional(readOnly = true)
    public List<CallRequest> getRequests(RequestStatus status) {
        if (status != null) {
            return callRequestRepository.findByStatusWithRequesterOrderByCreatedAtDesc(status);
        } else {
            return callRequestRepository.findAllWithRequesterOrderByCreatedAtDesc();
        }
    }

    @Transactional(readOnly = true)
    public CallRequest getRequestDetail(Long requestId) {
        return callRequestRepository.findByIdWithRequester(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));
    }

    @Transactional
    public MatchHistory acceptRequest(Long requestId, RequestAcceptRequestDto dto) {
        CallRequest callRequest = callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));

        if (callRequest.getStatus() != RequestStatus.WAITING) {
            throw new InvalidRequestStateException("현재 요청을 수락할 수 없습니다. (상태: " + callRequest.getStatus() + ")");
        }

        User provider = userRepository.findById(dto.providerId())
                .orElseThrow(() -> new ResourceNotFoundException("제공자를 찾을 수 없습니다."));

        if (callRequest.getRequester().getUserId().equals(provider.getUserId())) {
            throw new SelfAcceptNotAllowedException("자신의 요청을 수락할 수 없습니다.");
        }

        callRequest.markAsMatched();

        MatchHistory matchHistory = MatchHistory.create(callRequest, provider);
        return matchHistoryRepository.save(matchHistory);
    }

    @Transactional
    public CallRequest cancelRequest(Long requestId) {
        CallRequest callRequest = callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("요청을 찾을 수 없습니다."));

        if (callRequest.getStatus() != RequestStatus.WAITING && callRequest.getStatus() != RequestStatus.MATCHED) {
            throw new InvalidRequestStateException("현재 상태(" + callRequest.getStatus() + ")에서는 요청을 취소할 수 없습니다.");
        }

        callRequest.markAsCanceled();
        return callRequestRepository.save(callRequest);
    }

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
        matchHistory.markAsReturned(LocalDateTime.now());
        matchHistoryRepository.save(matchHistory);

        return callRequestRepository.save(callRequest);
    }
}
