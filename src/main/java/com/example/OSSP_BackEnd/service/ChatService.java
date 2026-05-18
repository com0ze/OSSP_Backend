package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.chat.ChatRoomCreateRequest;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomResponse;
import com.example.OSSP_BackEnd.entity.ChatRoom;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.repository.ChatRoomRepository;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신규 서비스: ChatService
 * 채팅 관련 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MatchHistoryRepository matchHistoryRepository;

    /**
     * 채팅방 생성 로직
     * @param request 채팅방 생성을 위한 matchId가 담긴 요청 DTO
     * @return 생성된 채팅방 정보를 담은 응답 DTO
     */
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request) {
        // 1. 요청 DTO에서 matchId를 가져와 MatchHistory 엔티티를 조회합니다.
        //    해당 ID의 MatchHistory가 없으면 ResourceNotFoundException을 발생시킵니다.
        MatchHistory matchHistory = matchHistoryRepository.findById(request.matchId())
                .orElseThrow(() -> new ResourceNotFoundException("MatchHistory not found with id: " + request.matchId()));

        // 2. 새로운 ChatRoom 엔티티를 생성합니다.
        //    정적 팩토리 메서드를 사용하여 객체 생성을 위임합니다.
        ChatRoom newChatRoom = ChatRoom.create(matchHistory);

        // 3. 생성된 ChatRoom을 데이터베이스에 저장합니다.
        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

        // 4. 저장된 ChatRoom 정보를 바탕으로 응답 DTO를 생성하여 반환합니다.
        return new ChatRoomResponse(
                savedChatRoom.getId(),
                savedChatRoom.getMatchHistory().getId(),
                savedChatRoom.getCreatedAt()
        );
    }
}
