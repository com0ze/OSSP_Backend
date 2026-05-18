package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 신규 리포지토리: ChatRoomRepository
 * ChatRoom 엔티티에 대한 데이터베이스 연산을 처리합니다.
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
