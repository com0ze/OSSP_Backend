package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.chat.ChatMessageResponse;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomCreateRequest;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomListResponse;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomResponse;
import com.example.OSSP_BackEnd.entity.ChatMessage;
import com.example.OSSP_BackEnd.entity.ChatRoom;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.exception.ResourceNotFoundException;
import com.example.OSSP_BackEnd.repository.ChatMessageRepository;
import com.example.OSSP_BackEnd.repository.ChatRoomRepository;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 특정 채팅방의 이전 메시지들을 페이징하여 조회합니다.
     *
     * @param roomId 메시지를 조회할 채팅방의 ID
     * @param pageable 페이징 및 정렬 정보를 담은 객체
     * @return 메시지 DTO들을 담은 Page 객체
     */
    public Page<ChatMessageResponse> getMessages(Long roomId, Pageable pageable) {
        // 1. 채팅방 존재 여부를 확인합니다. 존재하지 않으면 예외를 발생시킵니다.
        if (!chatRoomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("ChatRoom not found with id: " + roomId);
        }

        // 2. Repository를 호출하여 메시지 데이터를 페이징하여 가져옵니다.
        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomId(roomId, pageable);

        // 3. Entity Page를 DTO Page로 변환합니다.
        //    Page.map() 메서드를 사용하면 페이징 정보(총 페이지 수, 현재 페이지 등)는 그대로 유지하면서
        //    내부의 내용(List<ChatMessage>)만 변환(List<ChatMessageResponse>)할 수 있습니다.
        return messages.map(ChatMessageResponse::from);
    }


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

    /**
     * 현재 로그인한 사용자가 참여하고 있는 모든 채팅방 목록을 조회합니다.
     *
     * @param userId 현재 사용자의 ID
     * @return 각 채팅방의 상세 정보를 담은 DTO 목록
     */
    public List<ChatRoomListResponse> findMyChatRooms(Long userId) {
        // 1. Fetch Join을 사용하여 사용자가 참여한 모든 채팅방 정보를 한 번에 가져옵니다. (N+1 방지)
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(userId);

        if (chatRooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 조회된 채팅방들의 ID 목록을 추출합니다.
        List<Long> roomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .toList();

        // 3. 각 채팅방의 마지막 메시지를 한 번의 쿼리로 가져옵니다. (성능 최적화)
        List<ChatMessage> lastMessages = chatMessageRepository.findLastMessagesForRooms(roomIds);

        // 4. 메시지 목록을 채팅방 ID를 Key로 하는 Map으로 변환합니다. (O(1) 시간 복잡도로 조회를 위함)
        Map<Long, ChatMessage> lastMessageMap = lastMessages.stream()
                .collect(Collectors.toMap(message -> message.getChatRoom().getId(), Function.identity()));

        // 5. 채팅방 목록을 순회하며 최종 응답 DTO(ChatRoomListResponse)를 생성합니다.
        return chatRooms.stream()
                .map(chatRoom -> {
                    // 5-1. 현재 채팅방과 관련된 엔티티들을 가져옵니다. (Fetch Join으로 이미 로딩됨)
                    var matchHistory = chatRoom.getMatchHistory();
                    var callRequest = matchHistory.getRequest();
                    var requester = callRequest.getRequester();
                    var provider = matchHistory.getProvider();

                    // 5-2. 채팅 상대방을 판별합니다.
                    // 현재 사용자가 요청자(requester)이면, 상대방은 공급자(provider)입니다. 그 반대도 마찬가지입니다.
                    User opponent = requester.getId().equals(userId) ? provider : requester;

                    // 5-3. Map에서 현재 채팅방의 마지막 메시지를 찾습니다.
                    ChatMessage lastMessageEntity = lastMessageMap.get(chatRoom.getId());
                    String lastMessageContent = "아직 대화가 없습니다.";
                    LocalDateTime updatedAt = null;

                    if (lastMessageEntity != null) {
                        lastMessageContent = lastMessageEntity.getContent();
                        updatedAt = lastMessageEntity.getCreatedAt();
                    }

                    // 5-4. 최종 응답 DTO를 구성하여 반환합니다.
                    return new ChatRoomListResponse(
                            callRequest.getId(),
                            matchHistory.getId(),
                            chatRoom.getId(),
                            opponent.getId(),
                            opponent.getNickname(),
                            lastMessageContent,
                            updatedAt
                    );
                })
                .collect(Collectors.toList());
    }
}
