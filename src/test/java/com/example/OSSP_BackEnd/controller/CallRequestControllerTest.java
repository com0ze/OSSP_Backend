package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import com.example.OSSP_BackEnd.config.JwtAuthenticationFilter;
import com.example.OSSP_BackEnd.config.JwtTokenProvider;
import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.service.CallRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CallRequestController.class)
@Import(SecurityConfig.class)
@DisplayName("CallRequestController 테스트")
class CallRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CallRequestService callRequestService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private User requester;
    private User provider;
    private CallRequest callRequest;

    @BeforeEach
    void setUp() {
        requester = new User("requester", "requester@example.com", "password");
        requester.setId(1L);

        provider = new User("provider", "provider@example.com", "password");
        provider.setId(2L);

        callRequest = CallRequest.builder()
                .id(1L)
                .itemName("Item A")
                .buildingName("Building A")
                .rewardAmt(1000)
                .duration(60)
                .memo("Memo")
                .requester(requester)
                .status(RequestStatus.WAITING)
                .build();
    }

    @Test
    @DisplayName("[성공 케이스] 대여 요청 생성")
    void createRequest_success() throws Exception {
        // Given
        RequestCreateDto createDto = new RequestCreateDto("Item A", "Building A", 1000, 60, "Memo");
        given(callRequestService.createRequest(any(RequestCreateDto.class), any(Long.class)))
        .willReturn(callRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("대여 요청이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.requestId").value(callRequest.getId()))
                .andExpect(jsonPath("$.data.itemName").value(callRequest.getItemName()));
    }

    @Test
    @DisplayName("[성공 케이스] 대여 요청 수락")
    void acceptRequest_success() throws Exception {
        // Given
        Long requestId = 1L;
        RequestAcceptRequestDto acceptDto = new RequestAcceptRequestDto(provider.getId());
        MatchHistory matchHistory = MatchHistory.create(callRequest, provider);
        given(callRequestService.acceptRequest(eq(requestId), any(RequestAcceptRequestDto.class))).willReturn(matchHistory);

        // When & Then
        mockMvc.perform(post("/api/v1/requests/{requestId}/accept", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("대여 요청이 성공적으로 수락되었습니다."))
                .andExpect(jsonPath("$.data.matchId").value(matchHistory.getId()))
                .andExpect(jsonPath("$.data.requestId").value(matchHistory.getRequest().getId()))
                .andExpect(jsonPath("$.data.providerId").value(matchHistory.getProvider().getId()));
    }
}