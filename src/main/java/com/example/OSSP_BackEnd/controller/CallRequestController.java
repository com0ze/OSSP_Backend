package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.dto.response.ApiResponse;
import com.example.OSSP_BackEnd.dto.response.RequestAcceptDto;
import com.example.OSSP_BackEnd.dto.response.RequestResponseDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.service.CallRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.OSSP_BackEnd.dto.response.RequestDetailResponseDto;
import com.example.OSSP_BackEnd.dto.response.RequestListResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
