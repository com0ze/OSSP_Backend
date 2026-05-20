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
     * 유저 위치 정보 갱신
     * Ray-Casting 알고리즘을 사용하여 건물 내부 여부 판단
     * 
     * @param userId 사용자 ID
     * @param dto 위치 정보 (위도, 경도)
     */
    @Transactional
    public void updateLocation(Long userId, LocationUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // Building Enum의 findByCoordinate를 활용하여 건물 찾기
        Building building = Building.findByCoordinate(dto.latitude(), dto.longitude());

        // 건물이 발견되면 해당 건물 이름, 아니면 "OUTSIDE"로 설정
        String buildingName = (building != null) ? building.name() : "OUTSIDE";

        // User 엔티티의 위치 정보 업데이트
        user.updateLocation(dto.latitude(), dto.longitude(), buildingName);
    }
}
