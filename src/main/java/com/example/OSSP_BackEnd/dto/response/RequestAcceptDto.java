package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;

import java.time.LocalDateTime;

public record RequestAcceptDto(
        Long matchId,
        Long requestId,
        RequestStatus requestStatus,
        Long requesterId,
        Long providerId,
        String providerNickname,
        LocalDateTime matchedAt
) {

    public static RequestAcceptDto from(MatchHistory matchHistory) {
        return new RequestAcceptDto(
                matchHistory.getId(),
                matchHistory.getRequest().getId(),
                matchHistory.getRequest().getStatus(),
                matchHistory.getRequest().getRequester().getId(),
                matchHistory.getProvider().getId(),
                matchHistory.getProvider().getNickname(),
                matchHistory.getMatchedAt()
        );
    }
}