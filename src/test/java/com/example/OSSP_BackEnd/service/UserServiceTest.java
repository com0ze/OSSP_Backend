package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.response.MyProfileResponseDto;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

// @ExtendWith(MockitoExtension.class)는 JUnit 5에서 Mockito를 사용하기 위한 필수 설정입니다.
// 이 어노테이션을 통해 @Mock, @InjectMocks 등의 Mockito 어노테이션이 활성화됩니다.
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    // @InjectMocks는 실제 UserService 객체를 생성하고, 이 객체에 @Mock으로 정의된 Mock 객체들을 주입합니다.
    @InjectMocks
    private UserService userService;

    // @Mock은 가짜 객체(Mock 객체)를 생성합니다.
    // UserRepository는 UserService의 의존성이므로, 실제 데이터베이스에 접근하지 않고 Mock 객체를 사용합니다.
    @Mock
    private UserRepository userRepository;

    // 테스트에 사용할 공통 User 객체와 ID를 미리 선언합니다.
    private Long userId;
    private User user;

    // 각 테스트 메소드가 실행되기 전에 초기화 작업을 수행합니다.
    @BeforeEach
    void setUp() {
        userId = 1L;
        user = new User("테스트유저", "test@example.com", "password"); // 공개 생성자 사용
        user.setId(userId);
        user.setMannerScore(java.math.BigDecimal.valueOf(4.5));
    }

    @Test
    @DisplayName("getMyProfile - 내 프로필 조회 성공")
    void getMyProfile_Success() {
        // Given: Mock 객체(userRepository)의 동작을 정의합니다.
        // given(userRepository.findById(userId))는 userRepository의 findById 메서드가 userId로 호출될 때,
        // Optional.of(user)를 반환하도록 설정합니다. (사용자가 존재하는 경우를 모킹)
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When: 테스트 대상 메서드를 호출합니다.
        // userService.getMyProfile(userId)를 호출하여 실제 서비스 로직을 실행합니다.
        MyProfileResponseDto result = userService.getMyProfile(userId);

        // Then: 결과값을 검증합니다.
        // assertThat(result)를 사용하여 반환된 DTO가 null이 아닌지 확인합니다.
        assertThat(result).isNotNull();
        // assertThat(result.getNickname()).isEqualTo("테스트유저")를 사용하여 닉네임이 예상과 일치하는지 확인합니다.
        assertThat(result.getNickname()).isEqualTo("테스트유저");
        // assertThat(result.getMannerScore()).isEqualTo(4.5)를 사용하여 매너 점수가 예상과 일치하는지 확인합니다.
        assertThat(result.getMannerScore()).isEqualTo(4.5);

        // verify(userRepository).findById(userId)를 통해 userRepository의 findById 메서드가
        // userId 인자로 한 번 호출되었는지 검증합니다.
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("getMyProfile - 내 프로필 조회 실패 (사용자 없음)")
    void getMyProfile_NotFound() {
        // Given: Mock 객체(userRepository)의 동작을 정의합니다.
        // given(userRepository.findById(anyLong()))는 userRepository의 findById 메서드가 어떤 Long 타입 인자로 호출되든지,
        // Optional.empty()를 반환하도록 설정합니다. (사용자가 존재하지 않는 경우를 모킹)
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // When & Then: 특정 예외가 발생하는지 검증합니다.
        // assertThrows(ResourceNotFoundException.class, () -> userService.getMyProfile(userId))는
        // userService.getMyProfile(userId) 호출 시 ResourceNotFoundException이 발생하는지 확인합니다.
        assertThrows(ResourceNotFoundException.class, () -> userService.getMyProfile(userId));

        // verify(userRepository).findById(userId)를 통해 userRepository의 findById 메서드가
        // userId 인자로 한 번 호출되었는지 검증합니다.
        verify(userRepository).findById(userId);
    }
}
