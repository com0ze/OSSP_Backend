//프론트엔드에서 대여 요청(POST)을 보낼 때
//스프링이 그 데이터를 받아서 객체로 변환해주는 역할을 하는 코드

package com.example.OSSP_BackEnd.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallRequestCreateDto {
    
    @NotBlank(message = "물품명은 필수입니다")
    private String itemName;
    
    @NotBlank(message = "건물명은 필수입니다")
    private String buildingName;
    
    @NotNull(message = "보상 금액은 필수입니다")
    @Min(value = 0, message = "보상 금액은 0원 이상이어야 합니다")
    private Integer rewardAmt;
    
    @NotNull(message = "소요 시간은 필수입니다")
    @Min(value = 1, message = "소요 시간은 1분 이상이어야 합니다")
    private Integer duration;
    
    private String memo;
    
    @NotNull(message = "요청자 ID는 필수입니다")
    private Long requesterId;
}
