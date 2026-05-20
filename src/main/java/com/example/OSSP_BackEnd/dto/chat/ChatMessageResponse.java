package com.example.OSSP_BackEnd.dto.chat;

import com.example.OSSP_BackEnd.entity.ChatMessage;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {

    private final Long senderId;
    private final String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime createdAt;

    @Builder
    public ChatMessageResponse(Long senderId, String content, LocalDateTime createdAt) {
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .senderId(chatMessage.getSender().getId())
                .content(chatMessage.getContent())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
