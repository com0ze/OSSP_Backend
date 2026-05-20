package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 특정 사용자가 참여하고 있는 모든 채팅방을 조회합니다.
     * 이 때 Fetch Join을 사용하여 ChatRoom, MatchHistory, CallRequest, 각 User(requester, provider) 엔티티를
     * 한 번의 쿼리로 모두 가져와 N+1 문제를 방지합니다.
     *
     * @param userId 현재 로그인한 사용자의 ID
     * @return 사용자가 참여한 채팅방 엔티티 목록
     */
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN FETCH cr.matchHistory mh " +
            "JOIN FETCH mh.request crq " +
            "JOIN FETCH crq.requester req " +
            "JOIN FETCH mh.provider p " +
            "WHERE cr.id IN (SELECT DISTINCT cr2.id FROM ChatRoom cr2 JOIN cr2.matchHistory mh2 JOIN mh2.request crq2 WHERE crq2.requester.id = :userId OR mh2.provider.id = :userId)")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);
}