package com.example.OSSP_BackEnd.dto.chat;

import java.time.LocalDateTime;

/**
 * 신규 DTO: ChatRoomResponse
 * 채팅방 생성 후 응답으로 반환될 정보를 담는 레코드입니다.
 *
 * @param roomId 생성된 채팅방의 ID
 * @param matchId 채팅방과 연결된 매칭 ID
 * @param createdAt 채팅방 생성 시각
 */
public record ChatRoomResponse(
        Long roomId,
        Long matchId,
        LocalDateTime createdAt
) {
}
