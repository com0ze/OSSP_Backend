package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.response.MyProfileResponseDto;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest 어노테이션은 웹 계층(Controller) 테스트에 사용됩니다.
// Service, Repository 등 다른 계층의 빈들은 로드하지 않고, 웹 관련 빈들만 로드합니다.
// 테스트 대상 컨트롤러를 지정하여 해당 컨트롤러만 로드하도록 합니다.
@WebMvcTest(UserController.class)
@DisplayName("UserController 단위 테스트")
class UserControllerTest {

    // MockMvc는 HTTP 호출을 시뮬레이션하는 데 사용됩니다.
    // @Autowired를 통해 스프링 컨텍스트에서 주입받습니다.
    @Autowired
    private MockMvc mockMvc;

    // UserService는 UserController의 의존성이므로, 실제 UserService 대신 Mock 객체를 사용합니다.
    // @MockBean을 사용하면 스프링 컨텍스트에 Mockito Mock 객체가 등록되어 의존성 주입됩니다.
    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/v1/users/me - 내 프로필 조회 성공")
    // @WithMockUser는 Spring Security 컨텍스트에 가짜 인증된 사용자를 설정합니다.
    // username은 Principal의 getName()으로 사용되며, 여기서는 Long 타입의 userId로 가정합니다.
    // roles는 해당 유저가 가진 권한을 설정합니다.
    @WithMockUser(username = "1", roles = "USER")
    void getMyProfile_Success() throws Exception {
        // Given: Mock 객체(userService)의 동작을 정의합니다.
        // given(userService.getMyProfile(anyLong()))은 userService의 getMyProfile 메서드가 어떤 Long 타입 인자로 호출되든지
        // 특정 MyProfileResponseDto 객체를 반환하도록 설정합니다.
        MyProfileResponseDto expectedDto = MyProfileResponseDto.builder()
                .nickname("테스트유저")
                .mannerScore(BigDecimal.valueOf(5))
                .build();
        given(userService.getMyProfile(anyLong())).willReturn(expectedDto);

        // When & Then: MockMvc를 사용하여 HTTP 요청을 수행하고 결과를 검증합니다.
        // mockMvc.perform(get("/api/v1/users/me"))는 GET /api/v1/users/me 엔드포인트로 HTTP 요청을 보냅니다.
        mockMvc.perform(get("/api/v1/users/me"))
                // .andExpect(status().isOk())는 HTTP 응답 상태 코드가 200 OK인지 검증합니다.
                .andExpect(status().isOk())
                // .andExpect(jsonPath("$.nickname").value("테스트유저"))는 응답 JSON의 "nickname" 필드 값이 "테스트유저"인지 검증합니다.
                .andExpect(jsonPath("$.data.nickname").value("테스트유저"))
                // .andExpect(jsonPath("$.mannerScore").value(4.5))는 응답 JSON의 "mannerScore" 필드 값이 4.5인지 검증합니다.
                .andExpect(jsonPath("$.data.mannerScore").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - 내 프로필 조회 실패 (사용자 없음)")
    @WithMockUser(username = "999", roles = "USER") // 존재하지 않는 사용자 ID로 가정
    void getMyProfile_NotFound() throws Exception {
        // Given: Mock 객체(userService)의 동작을 정의합니다.
        // given(userService.getMyProfile(anyLong())).willThrow(...)는 userService의 getMyProfile 메서드가 호출될 때
        // ResourceNotFoundException 예외를 발생시키도록 설정합니다.
        given(userService.getMyProfile(anyLong()))
                .willThrow(new ResourceNotFoundException("실패"));

        // When & Then: MockMvc를 사용하여 HTTP 요청을 수행하고 결과를 검증합니다.
        mockMvc.perform(get("/api/v1/users/me"))
                // .andExpect(status().isNotFound())는 HTTP 응답 상태 코드가 404 Not Found인지 검증합니다.
                // 여기서는 예외 처리기가 적절히 404를 반환하는지만 확인하고,
                // GlobalExceptionHandler에서 반환하는 에러 응답 DTO의 상세 내용은 추가로 검증할 수 있습니다.
                // 예: .andExpect(jsonPath("$.message").value("User not found with id : '999'"));
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/users/me - 인증되지 않은 사용자 접근 거부")
    // @WithMockUser 어노테이션이 없는 경우, 스프링 시큐리티는 인증되지 않은 사용자로 간주합니다.
    void getMyProfile_Unauthorized() throws Exception {
        // Given: (별도의 Given 설정 없음, 서비스 호출 자체가 이루어지지 않음)

        // When & Then: 인증되지 않은 상태에서 요청을 보냅니다.
        mockMvc.perform(get("/api/v1/users/me"))
                // .andExpect(status().isUnauthorized())는 HTTP 응답 상태 코드가 401 Unauthorized인지 검증합니다.
                // 기본적으로 Spring Security가 인증되지 않은 요청에 대해 401을 반환합니다.
                .andExpect(status().isUnauthorized());
    }
}