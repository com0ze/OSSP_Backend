package com.example.OSSP_BackEnd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 및 STOMP 프로토콜을 위한 설정 클래스
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 프로토콜을 사용하여 클라이언트가 WebSocket에 연결할 수 있는 엔드포인트를 등록합니다.
     * @param registry STOMP 엔드포인트를 등록하기 위한 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/ws-stomp" 엔드포인트를 추가합니다. 클라이언트는 이 주소로 WebSocket 연결을 시작합니다.
        registry.addEndpoint("/ws-stomp")
                // CORS(Cross-Origin Resource Sharing)를 허용하여 모든 도메인에서의 연결을 허용합니다.
                .setAllowedOriginPatterns("*");
    }

    /**
     * 메시지 브로커를 설정하여 클라이언트 간의 메시지 라우팅을 구성합니다.
     * @param registry 메시지 브로커를 설정하기 위한 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지를 구독(subscribe)할 때 사용할 경로의 접두사(prefix)를 설정합니다.
        // "/topic"으로 시작하는 경로를 구독하는 클라이언트에게 메시지를 브로드캐스팅합니다.
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 서버로 메시지를 보낼 때(publish) 사용할 경로의 접두사를 설정합니다.
        // 예를 들어, 클라이언트가 "/app/chat.send"로 메시지를 보내면,
        // 해당 경로는 @MessageMapping 어노테이션이 붙은 컨트롤러 메서드로 라우팅됩니다.
        registry.setApplicationDestinationPrefixes("/app");
    }
}
