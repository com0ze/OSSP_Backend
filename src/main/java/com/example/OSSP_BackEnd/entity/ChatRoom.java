package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 신규 엔티티: ChatRoom
 * 채팅방 정보를 담는 엔티티입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    /**
     * ChatRoom과 MatchHistory의 연관 관계 설정입니다.
     * 실무에서는 아래와 같은 이유로 단방향 OneToOne 관계를 추천합니다.
     * 1. 단순성: MatchHistory에서 ChatRoom을 직접 조회할 일이 거의 없으므로, 단방향 관계로 충분합니다.
     * 2. 성능: 양방향 관계에서 발생할 수 있는 순환 참조나 불필요한 조회를 방지하고, LAZY 로딩으로 성능을 최적화합니다.
     * 3. 독립성: 각 엔티티의 책임이 명확해져 유지보수가 용이해집니다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchHistory matchHistory;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static ChatRoom create(MatchHistory matchHistory) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.matchHistory = matchHistory;
        return chatRoom;
    }
}
