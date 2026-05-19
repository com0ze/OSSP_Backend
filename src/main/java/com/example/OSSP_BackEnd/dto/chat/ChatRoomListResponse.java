package com.example.OSSP_BackEnd.dto.chat;

import java.time.LocalDateTime;

/**
 * 내 참여 채팅방 목록 조회 API의 응답 DTO
 *
 * @param requestId    원본 대여 요청 ID
 * @param matchId      매칭 ID
 * @param roomId       채팅방 ID
 * @param opponentId   채팅 상대방의 ID
 * @param opponentName 채팅 상대방의 닉네임
 * @param lastMessage  채팅방의 마지막 메시지 내용
 * @param updatedAt    마지막 메시지가 보내진 시간
 */
public record ChatRoomListResponse(
    Long requestId,
    Long matchId,
    Long roomId,
    Long opponentId,
    String opponentName,
    String lastMessage,
    LocalDateTime updatedAt
) {
}
