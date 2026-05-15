package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.dto.response.RequestResponseDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.exception.InvalidRequestStateException;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallRequestService {

    private final CallRequestRepository callRequestRepository;
    private final UserRepository userRepository;

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
    public RequestResponseDto acceptRequest(Long requestId) {
        CallRequest callRequest = getRequestOrThrow(requestId);
        validateCurrentStatus(callRequest, RequestStatus.MATCHED, RequestStatus.WAITING);

        callRequest.markAsMatched();
        return RequestResponseDto.from(callRequest);
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
        return RequestResponseDto.from(callRequest);
    }

    private CallRequest getRequestOrThrow(Long requestId) {
        return callRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("대여 요청을 찾을 수 없습니다. requestId=" + requestId));
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