package com.example.OSSP_BackEnd.dto.chat;

/**
 * 신규 DTO: ChatRoomCreateRequest
 * 채팅방 생성을 요청할 때 필요한 정보를 담는 레코드입니다.
 *
 * @param matchId 채팅방을 생성할 대상 매칭 ID
 */
public record ChatRoomCreateRequest(
        Long matchId
) {
}
