/*
실제로 로직이 돌아가는 코드
컨트롤러가 요청이 들어오고 주문서(CreateDto)를 주면,
여기서 상태를 WAITING으로 세팅하고,
창고지기(Repository)한테 저장하라고 시키는 로직을 짭니다.
 */
package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.CallRequestCreateDto;
import com.example.OSSP_BackEnd.dto.CallRequestResponseDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.CallStatus;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallRequestService {
    
    private final CallRequestRepository callRequestRepository;

    /**
     * 대여 요청 등록
     */
    @Transactional
    public CallRequestResponseDto createCallRequest(CallRequestCreateDto dto) {
        CallRequest callRequest = CallRequest.builder()
                .itemName(dto.getItemName())
                .buildingName(dto.getBuildingName())
                .rewardAmt(dto.getRewardAmt())
                .duration(dto.getDuration())
                .memo(dto.getMemo())
                .status(CallStatus.WAITING)
                .requesterId(dto.getRequesterId())
                .build();

        CallRequest savedRequest = callRequestRepository.save(callRequest);
        return CallRequestResponseDto.fromEntity(savedRequest);
    }

    /**
     * 주변 대여 요청 조회 (WAITING 상태인 요청들을 최신순으로)
     */
    public List<CallRequestResponseDto> getNearbyCallRequests() {
        List<CallRequest> waitingRequests = callRequestRepository
                .findByStatusOrderByCreatedAtDesc(CallStatus.WAITING);

        return waitingRequests.stream()
                .map(CallRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
