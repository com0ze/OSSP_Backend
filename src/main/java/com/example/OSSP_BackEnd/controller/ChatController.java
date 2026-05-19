package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.chat.ChatMessageResponse;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomCreateRequest;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomListResponse;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomResponse;
import com.example.OSSP_BackEnd.dto.response.ApiResponse;
import com.example.OSSP_BackEnd.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 특정 채팅방의 메시지 내역 페이징 조회 API
     *
     * @param roomId   메시지를 조회할 채팅방의 ID
     * @param pageable 페이징 및 정렬 정보 (기본값: 생성일시(createdAt) 기준 내림차순)
     * @return 메시지 목록과 페이징 정보를 담은 응답
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessages(
            @PathVariable Long roomId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 서비스에서 Page 객체를 받습니다.
        Page<ChatMessageResponse> messagePage = chatService.getMessages(roomId, pageable);
        // Page 객체에서 실제 데이터인 List만 추출하여 응답합니다.
        List<ChatMessageResponse> messages = messagePage.getContent();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅 메시지 목록을 성공적으로 조회했습니다.", messages));
    }


    /**
     * 채팅방 생성 API (POST /api/v1/chats)
     * @param request matchId를 포함하는 요청 Body
     * @return 생성된 채팅방 정보와 함께 201 Created 상태 코드를 반환합니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestBody ChatRoomCreateRequest request) {
        // ChatService를 호출하여 채팅방 생성 로직을 수행합니다.
        ChatRoomResponse chatRoomResponse = chatService.createChatRoom(request);

        // API 응답 형식에 맞추어 성공 응답을 생성하고, HTTP 상태 코드 201(Created)로 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "채팅방이 성공적으로 생성되었습니다.", chatRoomResponse));
    }

    /**
     * 내 참여 채팅방 목록 조회 API
     *
     * @param userId 현재 로그인한 사용자 ID
     *               (실제 프로덕션에서는 @AuthenticationPrincipal 등을 통해 인증 객체에서 가져옵니다)
     * @return 채팅방 목록 응답
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getMyChatRooms(
            // TODO: 추후 Spring Security 도입 시 @AuthenticationPrincipal User user 로 변경
            @RequestParam Long userId
    ) {
        List<ChatRoomListResponse> myChatRooms = chatService.findMyChatRooms(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "채팅방 목록을 성공적으로 조회했습니다.", myChatRooms));
    }
}
