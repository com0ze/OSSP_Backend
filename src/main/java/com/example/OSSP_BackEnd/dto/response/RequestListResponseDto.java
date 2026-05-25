package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RequestListResponseDto {
    private Long requestId;
    private Long requesterId;
    private String itemName;
    private String buildingName;
    private Integer rewardAmt;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private String requesterNickname;

    public static RequestListResponseDto of(CallRequest callRequest) {
        return RequestListResponseDto.builder()
                .requestId(callRequest.getId())
                .requesterId(callRequest.getRequester().getId())
                .itemName(callRequest.getItemName())
                .buildingName(callRequest.getBuildingName())
                .rewardAmt(callRequest.getRewardAmt())
                .status(callRequest.getStatus())
                .createdAt(callRequest.getCreatedAt())
                .requesterNickname(callRequest.getRequester().getNickname())
                .build();
    }
}