package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.dto.response.RequestAcceptDto;
import com.example.OSSP_BackEnd.dto.response.RequestResponseDto;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.service.CallRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/requests")
public class CallRequestController {

    private final CallRequestService callRequestService;

    @GetMapping
    public ResponseEntity<List<RequestResponseDto>> getRequests(
            @RequestParam(required = false) RequestStatus status
    ) {
        return ResponseEntity.ok(callRequestService.getRequests(status));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RequestResponseDto> getRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(callRequestService.getRequest(requestId));
    }

    @PostMapping
    public ResponseEntity<RequestResponseDto> createRequest(@Valid @RequestBody RequestCreateDto dto) {
        RequestResponseDto response = callRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<RequestAcceptDto> acceptRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestAcceptRequestDto dto
    ) {
        return ResponseEntity.ok(callRequestService.acceptRequest(requestId, dto));
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<RequestResponseDto> cancelRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(callRequestService.cancelRequest(requestId));
    }

    @PatchMapping("/{requestId}/handover")
    public ResponseEntity<RequestResponseDto> handoverRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(callRequestService.handoverRequest(requestId));
    }

    @PatchMapping("/{requestId}/complete")
    public ResponseEntity<RequestResponseDto> completeRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(callRequestService.completeRequest(requestId));
    }
}