// 상태를 나타내는 Enum 클래스
package com.example.OSSP_BackEnd.entity;

public enum CallStatus {
    WAITING,    // 대기 중
    MATCHED,    // 매칭됨
    IN_USE,     // 사용 중
    COMPLETED,  // 완료됨
    CANCELED    // 취소됨
}
