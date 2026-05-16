// 서버가 일을 다 끝내고 프론트엔드 화면에 뿌려줄 데이터를 담아내는 상자

package com.example.OSSP_BackEnd.dto;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus; // 💡 임포트 수정: CallStatus -> RequestStatus
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallRequestResponseDto {
    private Long id;
    private String itemName;
    private String buildingName;
    private Integer rewardAmt;
    private Integer duration;
    private String memo;
    private RequestStatus status; // 💡 타입 수정: CallStatus -> RequestStatus
    private LocalDateTime createdAt;
    private Long requesterId;

    // Entity -> DTO 변환 메서드
    public static CallRequestResponseDto fromEntity(CallRequest callRequest) {
        return CallRequestResponseDto.builder()
                .id(callRequest.getId())
                .itemName(callRequest.getItemName())
                .buildingName(callRequest.getBuildingName())
                .rewardAmt(callRequest.getRewardAmt())
                .duration(callRequest.getDuration())
                .memo(callRequest.getMemo())
                .status(callRequest.getStatus())
                .createdAt(callRequest.getCreatedAt())
                .requesterId(callRequest.getRequester().getUserId()) 
                .build();
    }
}