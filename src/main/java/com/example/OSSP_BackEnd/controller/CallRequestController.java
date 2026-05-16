package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.ApiResponse;
import com.example.OSSP_BackEnd.dto.CallRequestCreateDto;
import com.example.OSSP_BackEnd.dto.CallRequestResponseDto;
import com.example.OSSP_BackEnd.service.CallRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class CallRequestController {

    private final CallRequestService callRequestService;

    /**
     * 대여 요청 등록
     * POST /api/v1/requests
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CallRequestResponseDto>> createCallRequest(
            @Valid @RequestBody CallRequestCreateDto dto) {
        
        CallRequestResponseDto response = callRequestService.createCallRequest(dto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("대여 요청이 성공적으로 등록되었습니다.", response));
    }

    /**
     * 주변 대여 요청 조회 (WAITING 상태인 요청들을 최신순으로)
     * GET /api/v1/requests/nearby
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<CallRequestResponseDto>>> getNearbyCallRequests() {
        
        List<CallRequestResponseDto> requests = callRequestService.getNearbyCallRequests();
        
        return ResponseEntity.ok(
                ApiResponse.success("주변 대여 요청 목록을 성공적으로 조회했습니다.", requests)
        );
    }
}
