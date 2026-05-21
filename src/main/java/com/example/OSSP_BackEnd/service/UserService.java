package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.DeviceTokenRequestDto;
import com.example.OSSP_BackEnd.dto.request.DutyUpdateRequestDto;
import com.example.OSSP_BackEnd.dto.request.LocationUpdateRequestDto;
import com.example.OSSP_BackEnd.dto.response.UserProfileResponseDto;
import com.example.OSSP_BackEnd.dto.response.MyProfileResponseDto;
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

    /**
     * FCM 기기 토큰 등록/갱신
     * 
     * @param userId 사용자 ID
     * @param dto FCM 토큰 정보
     */
    @Transactional
    public void updateDeviceToken(Long userId, DeviceTokenRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // User 엔티티의 FCM 토큰 업데이트
        user.updateDeviceToken(dto.fcmToken());
    }

    /**
     * 알림 받기 ON/OFF 상태 변경
     * 
     * @param userId 사용자 ID
     * @param dto 알림 받기 설정 정보
     */
    @Transactional
    public void updateDutyStatus(Long userId, DutyUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        // User 엔티티의 알림 받기 상태 업데이트
        user.updateDutyStatus(dto.isOnDuty());
    }

    /**
     * 특정 유저 프로필 조회
     * 민감한 정보(password, deviceToken)는 제외하고 안전한 정보만 반환
     * 
     * @param userId 조회할 사용자 ID
     * @return UserProfileResponseDto 유저 프로필 정보
     */
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        return UserProfileResponseDto.from(user);
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보 조회
     * API 명세에 따라 닉네임과 매너 점수만 반환합니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return MyProfileResponseDto 현재 사용자의 닉네임과 매너 점수 정보
     */
    public MyProfileResponseDto getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("현재 로그인한 사용자를 찾을 수 없습니다.")); // 예외 메시지 구체화

        return MyProfileResponseDto.from(user);
    }
}

