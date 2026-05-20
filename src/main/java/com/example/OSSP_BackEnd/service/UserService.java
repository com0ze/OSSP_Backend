package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.LocationUpdateRequestDto;
import com.example.OSSP_BackEnd.entity.Building;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 유저 위치 정보 갱신 (경량화 버전)
     * 프론트엔드에서 지오펜싱 처리 후 건물명만 받아서 저장
     * 
     * @param userId 사용자 ID
     * @param dto 건물 정보
     */
    @Transactional
    public void updateLocation(Long userId, LocationUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // 건물명을 Building Enum으로 변환하여 유효성 검증
        Building building = Building.fromString(dto.currentBuilding());
        
        // 유효한 건물인 경우 Enum 상수명을 저장, 그렇지 않으면 "OUTSIDE" 저장
        String buildingName = (building != null) ? building.name() : "OUTSIDE";

        // User 엔티티의 건물 정보 업데이트
        user.updateCurrentBuilding(buildingName);
    }
}
