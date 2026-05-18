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
    private Integer rewardAmt;
    private Integer duration;
    private String memo;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private Long requesterId;
    private String requesterNickname;

    public static RequestDetailResponseDto of(CallRequest callRequest) {
        return RequestDetailResponseDto.builder()
                .requestId(callRequest.getId())
                .itemName(callRequest.getItemName())
                .buildingName(callRequest.getBuildingName())
                .rewardAmt(callRequest.getRewardAmt())
                .duration(callRequest.getDuration())
                .memo(callRequest.getMemo())
                .status(callRequest.getStatus())
                .createdAt(callRequest.getCreatedAt())
                .requesterId(callRequest.getRequester().getId())
                .requesterNickname(callRequest.getRequester().getNickname())
                .build();
    }
}