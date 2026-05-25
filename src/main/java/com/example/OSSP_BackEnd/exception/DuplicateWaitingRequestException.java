package com.example.OSSP_BackEnd.exception;

/**
 * 도배 방지 예외
 * 수요자가 이미 WAITING 상태의 요청을 가지고 있을 때 발생
 */
public class DuplicateWaitingRequestException extends RuntimeException {
    
    public DuplicateWaitingRequestException(String message) {
        super(message);
    }
    
    public DuplicateWaitingRequestException() {
        super("이미 대기 중인 요청이 있습니다. 기존 요청이 완료된 후 다시 시도해주세요.");
    }
}
