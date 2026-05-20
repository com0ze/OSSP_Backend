package com.example.OSSP_BackEnd.dto.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 클라이언트가 WebSocket을 통해 채팅 메시지를 보낼 때 사용하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRequest {

    /**
     * 메시지가 속한 채팅방의 ID
     */
    private Long roomId;

    /**
     * 메시지를 보낸 사용자의 ID
     */
    private Long senderId;

    /**
     * 메시지 내용
     */
    private String content;
}
