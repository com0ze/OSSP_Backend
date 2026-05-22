package com.example.OSSP_BackEnd.exception;

import com.example.OSSP_BackEnd.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     비즈니스 예외: 올바르지 않은 상태에서 변경 시도 시 (InvalidRequestStateException)
     */
    @ExceptionHandler(InvalidRequestStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidRequestState(InvalidRequestStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.success(409, ex.getMessage(), null));
    }

    /*
    비즈니스 예외: 본인이 올린 호출글을 본인이 수락하려고 할 때 (SelfAcceptNotAllowedException)
     */
    @ExceptionHandler(SelfAcceptNotAllowedException.class)
    public ResponseEntity<ApiResponse<Object>> handleSelfAcceptNotAllowed(SelfAcceptNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.success(400, ex.getMessage(), null));
    }

    /*
    비즈니스 예외: 요청한 데이터(글, 유저 등)가 존재하지 않을 때 (ResourceNotFoundException)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.success(404, ex.getMessage(), null));
    }

    /*
    Spring 내장 예외: @Valid 유효성 검사 실패 시 (DTO 파라미터 누락 등)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.success(400, message, null));
    }

    /*
    Spring 내장 예외: 엔티티 속성 및 파라미터 제약조건 위반 시
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.success(400, message, null));
    }

    /*
    Spring 내장 예외: HTTP 요청 바디(JSON) 형식이 아예 맞지 않거나 읽을 수 없을 때
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.success(400, "요청 본문(JSON) 형식이 올바르지 않습니다.", null));
    }

    /*
    Spring 내장 예외: 쿼리 파라미터 타입 불일치 및 필수 파라미터 누락 시
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(Exception ex) {
        String message = "요청 파라미터가 올바르지 않습니다. status는 WAITING, MATCHED, CANCELED, IN_USE, COMPLETED 중 하나여야 합니다.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.success(400, message, null));
    }

/*
    최후의 보루: 위에서 걸러지지 않은 시스템 내부의 모든 500 에러 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAnyException(Exception ex) {
        ex.printStackTrace(); 
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.success(500, "서버 처리 중 오류가 발생했습니다.", null));
    }
}