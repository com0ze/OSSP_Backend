package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;

import java.time.LocalDateTime;

public record RequestResponseDto(
        Long requestId,
        String itemName,
        String buildingName,
        Integer rewardAmt,
        Integer duration,
        String memo,
        RequestStatus status,
        LocalDateTime createdAt,
        String requesterNickname
) {

    public static RequestResponseDto from(CallRequest request) {
        return new RequestResponseDto(
                request.getId(),
                request.getItemName(),
                request.getBuildingName(),
                request.getRewardAmt(),
                request.getDuration(),
                request.getMemo(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getRequester().getNickname()
        );
    }
}