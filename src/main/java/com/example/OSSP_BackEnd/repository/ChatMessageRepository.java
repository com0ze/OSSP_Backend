package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 여러 채팅방의 마지막 메시지를 한 번의 쿼리로 조회합니다.
     *
     * [성능 최적화 이유]
     * 1. IN 절과 서브쿼리 사용: 각 채팅방마다 마지막 메시지를 가져오기 위해 루프를 돌며 쿼리를 실행하는 대신,
     *    IN 절과 서브쿼리를 사용하여 단 한 번의 쿼리로 모든 필요한 데이터를 가져옵니다.
     * 2. 서브쿼리 최적화: `GROUP BY`를 통해 각 `room_id` 별로 가장 큰 `message_id` (가장 최근 메시지)를 찾고,
     *    메인 쿼리에서 해당 `message_id`를 가진 메시지 정보들만 효율적으로 조회합니다.
     *    이는 JPA 환경에서 "N개 그룹의 Top 1 레코드"를 가져오는 가장 표준적이고 효율적인 방법 중 하나입니다.
     *
     * @param roomIds 마지막 메시지를 조회할 채팅방 ID 목록
     * @return 각 채팅방의 마지막 메시지 엔티티 목록
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.id IN " +
            "(SELECT MAX(cm2.id) FROM ChatMessage cm2 WHERE cm2.chatRoom.id IN :roomIds GROUP BY cm2.chatRoom.id)")
    List<ChatMessage> findLastMessagesForRooms(@Param("roomIds") List<Long> roomIds);
}
