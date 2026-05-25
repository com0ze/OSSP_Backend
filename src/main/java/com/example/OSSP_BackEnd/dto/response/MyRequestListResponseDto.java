package com.example.OSSP_BackEnd.dto.response;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyRequestListResponseDto {
    private Long requestId;
    private String itemName;
    private String buildingName;
    private Integer rewardAmt;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private String requesterNickname;
    private Long requesterId;
    private Long matchId;
    private Long providerId;

    public static MyRequestListResponseDto of(CallRequest callRequest) {
        MyRequestListResponseDtoBuilder builder = MyRequestListResponseDto.builder()
                .requestId(callRequest.getId())
                .itemName(callRequest.getItemName())
                .buildingName(callRequest.getBuildingName())
                .rewardAmt(callRequest.getRewardAmt())
                .status(callRequest.getStatus())
                .createdAt(callRequest.getCreatedAt())
                .requesterNickname(callRequest.getRequester().getNickname());

        RequestStatus status = callRequest.getStatus();

        // WAITING, CANCELED 상태일 경우 requesterId만 포함
        if (status == RequestStatus.WAITING || status == RequestStatus.CANCELED) {
            builder.requesterId(callRequest.getRequester().getId());
        } 
        // MATCHED, IN_USE, COMPLETED 상태일 경우, 매칭 정보 포함
        else if (status == RequestStatus.MATCHED || status == RequestStatus.IN_USE || status == RequestStatus.COMPLETED) {
            builder.requesterId(callRequest.getRequester().getId());
            
            MatchHistory matchHistory = callRequest.getMatchHistory();
            // NullPointerException 방지를 위해 matchHistory 객체가 null이 아닌지 확인
            if (matchHistory != null) {
                builder.matchId(matchHistory.getId());
                // provider 객체도 null이 아닌지 확인하여 안정성 강화
                if (matchHistory.getProvider() != null) {
                    builder.providerId(matchHistory.getProvider().getId());
                }
            }
        }

        return builder.build();
    }
}
