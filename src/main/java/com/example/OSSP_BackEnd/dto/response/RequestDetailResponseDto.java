package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RequestDetailResponseDto {
    private Long requestId;
    private String itemName;
    private String buildingName;
    private String rewardAmt;
    private String duration;
    private String memo;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private Long requesterId;
    private String requesterNickname;

    public static RequestDetailResponseDto of(CallRequest callRequest) {
        return RequestDetailResponseDto.builder()
                .requestId(callRequest.getRequestId())
                .itemName(callRequest.getItemName())
                .buildingName(callRequest.getBuildingName())
                .rewardAmt(callRequest.getRewardAmt())
                .duration(callRequest.getDuration())
                .memo(callRequest.getMemo())
                .status(callRequest.getStatus())
                .createdAt(callRequest.getCreatedAt())
                .requesterId(callRequest.getRequester().getUserId())
                .requesterNickname(callRequest.getRequester().getNickname())
                .build();
    }
}
