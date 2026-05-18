package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.dto.response.*;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.service.CallRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class CallRequestController {

    private final CallRequestService callRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<RequestResponseDto>> createRequest(@RequestBody @Valid RequestCreateDto dto) {
        CallRequest callRequest = callRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED,
                "대여 요청이 성공적으로 생성되었습니다.",
                RequestResponseDto.from(callRequest)
        ));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<RequestListResponseDto>>> getRequests(
            @RequestParam(required = false) RequestStatus status
    ) {
        List<RequestListResponseDto> requests = callRequestService.getRequests(status).stream()
                .map(RequestListResponseDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "대여 요청 목록을 성공적으로 조회했습니다.",
                requests
        ));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<RequestListResponseDto>>> getMyRequests(
            @RequestParam(required = false, defaultValue = "active") String type
    ) {
        // TODO: 시큐리티 인증 구현 후 실제 로그인한 유저 ID로 변경
        Long currentUserId = 1L; // 임시 하드코딩
        
        List<RequestListResponseDto> requests = callRequestService.getMyRequests(currentUserId, type).stream()
                .map(RequestListResponseDto::of)
                .collect(Collectors.toList());
        
        String message = "active".equalsIgnoreCase(type) 
                ? "진행 중인 대여 요청 목록을 성공적으로 조회했습니다."
                : "과거 대여 요청 목록을 성공적으로 조회했습니다.";
        
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                message,
                requests
        ));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<RequestListResponseDto>>> getNearbyCallRequests() {
        // 💡 팀원의 서비스 메서드 구조에 맞추거나, 강현님이 짠 서비스 메서드를 호출하도록 연결해야 합니다.
        List<RequestListResponseDto> requests = callRequestService.getRequests(RequestStatus.WAITING).stream()
                .map(RequestListResponseDto::of)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "주변 대여 요청 목록을 성공적으로 조회했습니다.",
                requests
        ));
    }


    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<RequestDetailResponseDto>> getRequestDetail(@PathVariable Long requestId) {
        CallRequest callRequest = callRequestService.getRequestDetail(requestId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "대여 요청 상세 정보를 성공적으로 조회했습니다.",
                RequestDetailResponseDto.of(callRequest)
        ));
    }


    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<RequestAcceptDto>> acceptRequest(
            @PathVariable Long requestId,
            @RequestBody @Valid RequestAcceptRequestDto dto
    ) {
        MatchHistory matchHistory = callRequestService.acceptRequest(requestId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED,
                "대여 요청이 성공적으로 수락되었습니다.",
                RequestAcceptDto.from(matchHistory)
        ));
    }


    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(@PathVariable Long requestId) {
        callRequestService.cancelRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "대여 요청이 성공적으로 취소되었습니다."));
    }


    @PatchMapping("/{requestId}/handover")
    public ResponseEntity<ApiResponse<Void>> handoverItem(@PathVariable Long requestId) {
        callRequestService.handoverItem(requestId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "물품 전달이 완료되었습니다."));
    }

    @PatchMapping("/{requestId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeRequest(@PathVariable Long requestId) {
        callRequestService.completeRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "거래가 성공적으로 완료되었습니다."));
    }
}