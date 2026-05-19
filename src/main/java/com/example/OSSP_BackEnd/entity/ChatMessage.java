package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 신규 엔티티: ChatMessage
 * 향후 채팅 메시지 저장을 위해 미리 생성하는 엔티티입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    /**
     * ChatMessage는 특정 ChatRoom에 속하므로 ManyToOne 관계를 설정합니다.
     * LAZY 로딩으로 설정하여 메시지 조회 시 불필요한 채팅방 정보까지 가져오지 않도록 합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * 메시지를 보낸 사용자와의 관계입니다.
     * 마찬가지로 LAZY 로딩을 적용합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
