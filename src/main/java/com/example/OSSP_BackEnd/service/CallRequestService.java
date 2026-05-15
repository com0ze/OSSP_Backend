package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.dto.response.RequestAcceptDto;
import com.example.OSSP_BackEnd.dto.response.RequestResponseDto;
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
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallRequestService {

    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;
    private final MatchHistoryRepository matchHistoryRepository;

    public List<RequestResponseDto> getRequests(RequestStatus status) {
        List<CallRequest> requests = status == null
                ? callRequestRepository.findAllByOrderByCreatedAtDesc()
                : callRequestRepository.findByStatusOrderByCreatedAtDesc(status);

        return requests.stream()
                .map(RequestResponseDto::from)
                .toList();
    }

    public RequestResponseDto getRequest(Long requestId) {
        return RequestResponseDto.from(getRequestOrThrow(requestId));
    }

    @Transactional
    public RequestResponseDto createRequest(RequestCreateDto dto) {
        User requester = userRepository.findById(dto.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException("요청자를 찾을 수 없습니다. requesterId=" + dto.requesterId()));

        CallRequest callRequest = CallRequest.create(
                dto.itemName(),
                dto.buildingName(),
                dto.rewardAmt(),
                dto.duration(),
                dto.memo(),
                requester
        );

        CallRequest savedRequest = callRequestRepository.save(callRequest);
        return RequestResponseDto.from(savedRequest);
    }

    @Transactional
    public RequestAcceptDto acceptRequest(Long requestId, RequestAcceptRequestDto dto) {
        CallRequest callRequest = getRequestOrThrow(requestId);
        User provider = getUserOrThrow(dto.providerId(), "수락자를 찾을 수 없습니다. providerId=" + dto.providerId());

        validateCurrentStatus(callRequest, RequestStatus.MATCHED, RequestStatus.WAITING);
        validateNotSelfAccept(callRequest, provider);

        callRequest.markAsMatched();
        MatchHistory matchHistory = matchHistoryRepository.save(MatchHistory.create(callRequest, provider));

        return RequestAcceptDto.from(matchHistory);
    }

    @Transactional
    public RequestResponseDto cancelRequest(Long requestId) {
        CallRequest callRequest = getRequestOrThrow(requestId);
        validateCurrentStatus(callRequest, RequestStatus.CANCELED, RequestStatus.WAITING, RequestStatus.MATCHED);

        callRequest.markAsCanceled();
        return RequestResponseDto.from(callRequest);
    }

    @Transactional
    public RequestResponseDto handoverRequest(Long requestId) {
        CallRequest callRequest = getRequestOrThrow(requestId);
        validateCurrentStatus(callRequest, RequestStatus.IN_USE, RequestStatus.MATCHED);

        callRequest.markAsInUse();
        return RequestResponseDto.from(callRequest);
    }

    @Transactional
    public RequestResponseDto completeRequest(Long requestId) {
        CallRequest callRequest = getRequestOrThrow(requestId);
        validateCurrentStatus(callRequest, RequestStatus.COMPLETED, RequestStatus.IN_USE);

        callRequest.markAsCompleted();
        MatchHistory matchHistory = getMatchHistoryByRequestIdOrThrow(requestId);
        matchHistory.markAsReturned(LocalDateTime.now());

        return RequestResponseDto.from(callRequest);
    }

    private CallRequest getRequestOrThrow(Long requestId) {
        return callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("대여 요청을 찾을 수 없습니다. requestId=" + requestId));
    }

    private User getUserOrThrow(Long userId, String message) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private MatchHistory getMatchHistoryByRequestIdOrThrow(Long requestId) {
        return matchHistoryRepository.findByRequest_RequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("매칭 이력을 찾을 수 없습니다. requestId=" + requestId));
    }

    private void validateNotSelfAccept(CallRequest callRequest, User provider) {
        Long requesterId = callRequest.getRequester().getUserId();
        Long providerId = provider.getUserId();

        if (requesterId.equals(providerId)) {
            throw new SelfAcceptNotAllowedException("본인이 작성한 대여 요청은 수락할 수 없습니다. userId=" + providerId);
        }
    }

    private void validateCurrentStatus(
            CallRequest callRequest,
            RequestStatus nextStatus,
            RequestStatus... allowedCurrentStatuses
    ) {
        RequestStatus currentStatus = callRequest.getStatus();
        boolean isAllowed = Arrays.asList(allowedCurrentStatuses).contains(currentStatus);

        if (!isAllowed) {
            throw new InvalidRequestStateException(String.format(
                    "대여 요청 상태를 %s에서 %s(으)로 변경할 수 없습니다. 허용되는 이전 상태: %s",
                    currentStatus,
                    nextStatus,
                    Arrays.toString(allowedCurrentStatuses)
            ));
        }
    }
}