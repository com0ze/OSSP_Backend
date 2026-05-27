package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.dto.response.*;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.security.CustomUserDetails;
import com.example.OSSP_BackEnd.service.CallRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class CallRequestController {

    private final CallRequestService callRequestService;

    /**
     * 대여 요청 생성
     * @param dto 대여 요청 생성에 필요한 정보
     * @return 생성된 대여 요청 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RequestResponseDto>> createRequest(@RequestBody @Valid RequestCreateDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        CallRequest callRequest = callRequestService.createRequest(dto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED,
                "대여 요청이 성공적으로 생성되었습니다.",
                RequestResponseDto.from(callRequest)
        ));
    }

    /**
     * 대여 요청 목록 조회
     * @param status 조회할 대여 요청 상태 (선택 사항)
     * @return 대여 요청 목록
     */
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

    /**
     * 내 대여 요청 목록 조회
     * @return 내 대여 요청 목록
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<MyRequestListResponseDto>>> getMyRequests(
            @RequestParam(required = false) RequestStatus status
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long currentUserId = userDetails.getId();

        List<MyRequestListResponseDto> requests = callRequestService.getMyAndAcceptedRequestsByStatus(currentUserId, status).stream()
                .map(MyRequestListResponseDto::of)
                .collect(Collectors.toList());

        String message = "내 대여 및 수락 요청 목록을 성공적으로 조회했습니다.";
        if (status != null) {
            message = String.format("'%s' 상태의 내 대여 및 수락 요청 목록을 성공적으로 조회했습니다.", status);
        }

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                message,
                requests
        ));
    }

    /**
     * 주변 대여 요청 목록 조회
     * @return 주변 대여 요청 목록
     */
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

    /**
     * 대여 요청 상세 정보 조회
     * @param requestId 조회할 대여 요청 ID
     * @return 대여 요청 상세 정보
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<RequestDetailResponseDto>> getRequestDetail(@PathVariable Long requestId) {
        CallRequest callRequest = callRequestService.getRequestDetail(requestId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "대여 요청 상세 정보를 성공적으로 조회했습니다.",
                RequestDetailResponseDto.of(callRequest)
        ));
    }

    /**
     * 대여 요청 수락
     * @param requestId 수락할 대여 요청 ID
     * @param dto 대여 요청 수락에 필요한 정보
     * @return 매칭 기록 정보
     */
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

    /**
     * 대여 요청 취소
     * @param requestId 취소할 대여 요청 ID
     * @return 성공 응답
     */
    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(@PathVariable Long requestId) {
        callRequestService.cancelRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "대여 요청이 성공적으로 취소되었습니다."));
    }

    /**
     * 물품 전달 완료 처리
     * @param requestId 물품 전달을 완료할 대여 요청 ID
     * @return 성공 응답
     */
    @PatchMapping("/{requestId}/handover")
    public ResponseEntity<ApiResponse<Void>> handoverItem(@PathVariable Long requestId) {
        callRequestService.handoverItem(requestId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "물품 전달이 완료되었습니다."));
    }

    /**
     * 거래 완료 처리
     * @param requestId 거래를 완료할 대여 요청 ID
     * @return 성공 응답
     */
    @PatchMapping("/{requestId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeRequest(@PathVariable Long requestId) {
        callRequestService.completeRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "거래가 성공적으로 완료되었습니다."));
    }
}