package com.example.OSSP_BackEnd.controller;

import com.example.OSSP_BackEnd.dto.chat.ChatRoomCreateRequest;
import com.example.OSSP_BackEnd.dto.chat.ChatRoomResponse;
import com.example.OSSP_BackEnd.dto.response.ApiResponse;
import com.example.OSSP_BackEnd.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 신규 컨트롤러: ChatController
 * 채팅 관련 API 요청을 처리합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

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
}
