package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.dto.request.RequestAcceptRequestDto;
import com.example.OSSP_BackEnd.dto.request.RequestCreateDto;
import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.MatchHistory;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.exception.SelfAcceptNotAllowedException;
import com.example.OSSP_BackEnd.repository.CallRequestRepository;
import com.example.OSSP_BackEnd.repository.MatchHistoryRepository;
import com.example.OSSP_BackEnd.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("CallRequestService 통합 테스트")
class CallRequestServiceIntegrationTest {

    @Autowired
    CallRequestService callRequestService;

    @Autowired
    CallRequestRepository callRequestRepository;

    @Autowired
    MatchHistoryRepository matchHistoryRepository;

    @Autowired
    UserRepository userRepository;

    private User createUser(String nickname) {
        User user = new User(nickname, nickname + "@example.com", "password");
        return userRepository.save(user);
    }

private CallRequest createCallRequest(User requester, String itemName, RequestStatus status) {
        // 💡 해결책: 존재하지 않는 create() 대신 롬복 @Builder 사용 및 타입 일치화
        CallRequest callRequest = CallRequest.builder()
                .itemName(itemName)
                .buildingName("Building A")
                .rewardAmt(1000)
                .duration(60)
                .memo("Memo")
                .requester(requester)
                .status(status)
                .build();

        return callRequestRepository.save(callRequest);
    }

    @Test
    @DisplayName("[성공 케이스] 대여 요청 수락")
    void acceptRequest_success() {
        // Given
        User requester = createUser("requester");
        User provider = createUser("provider");
        CallRequest callRequest = createCallRequest(requester, "Item A", RequestStatus.WAITING);

        RequestAcceptRequestDto dto = new RequestAcceptRequestDto(provider.getUserId());

        // When
        MatchHistory acceptedMatchHistory = callRequestService.acceptRequest(callRequest.getId(), dto);

        // Then
        CallRequest foundRequest = callRequestRepository.findById(callRequest.getId()).orElseThrow();
        assertThat(foundRequest.getStatus()).isEqualTo(RequestStatus.MATCHED);
        assertThat(acceptedMatchHistory.getProvider().getUserId()).isEqualTo(provider.getUserId());
        assertThat(acceptedMatchHistory.getRequest().getId()).isEqualTo(callRequest.getId());
    }

    @Test
    @DisplayName("[성공 케이스] 거래 완료 처리")
    void completeRequest_success() {
        // Given
        User requester = createUser("requester");
        User provider = createUser("provider");
        CallRequest callRequest = createCallRequest(requester, "Item B", RequestStatus.IN_USE);

        MatchHistory matchHistory = MatchHistory.create(callRequest, provider);
        matchHistoryRepository.save(matchHistory);

        // When
        callRequestService.completeRequest(callRequest.getId());

        // Then
        MatchHistory foundMatchHistory = matchHistoryRepository.findByRequestIdWithRequest(callRequest.getId()).orElseThrow();
        assertThat(foundMatchHistory.getReturnedAt()).isNotNull();
        assertThat(foundMatchHistory.getRequest().getStatus()).isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    @DisplayName("[실패 케이스] 본인 요청 수락 방지")
    void acceptRequest_selfAcceptNotAllowed() {
        // Given
        User requester = createUser("requester");
        CallRequest callRequest = createCallRequest(requester, "Item C", RequestStatus.WAITING);

        RequestAcceptRequestDto dto = new RequestAcceptRequestDto(requester.getUserId());

        // When & Then
        assertThatThrownBy(() -> callRequestService.acceptRequest(callRequest.getId(), dto))
                .isInstanceOf(SelfAcceptNotAllowedException.class)
                .hasMessageContaining("자신의 요청을 수락할 수 없습니다.");
    }

    /*
     * application-test.yml 설정 가이드
     *
     * src/main/resources/application-test.yml 파일을 생성하고 다음 내용을 추가합니다.
     * spring:
     *   datasource:
     *     url: jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC
     *     username: your_test_username
     *     password: your_test_password
     *     driver-class-name: com.mysql.cj.jdbc.Driver
     *   jpa:
     *     hibernate:
     *       ddl-auto: create-drop # 테스트 종료 후 스키마를 정리합니다.
     *     properties:
     *       hibernate.dialect: org.hibernate.dialect.MySQL8Dialect
     *     show-sql: true
     *   h2:
     *     console:
     *       enabled: true # H2 데이터베이스를 사용할 경우 콘솔을 활성화할 수 있습니다.
     *
     * 테스트 실행 시, Spring Boot는 기본적으로 application.properties 또는 application.yml 파일을 로드합니다.
     * 그러나 @SpringBootTest 어노테이션과 함께 @ActiveProfiles("test")를 사용하여
     * application-test.yml 파일을 명시적으로 로드하도록 할 수 있습니다.
     * 예시:
     * @SpringBootTest
     * @ActiveProfiles("test")
     * class CallRequestServiceIntegrationTest {
     *     // ...
     * }
     *
     * 이렇게 설정하면 테스트 환경에서는 application-test.yml에 정의된 데이터베이스 설정을 사용하고,
     * 실제 애플리케이션 실행 시에는 application.yml (또는 .properties) 설정을 사용하게 됩니다.
     */
}
