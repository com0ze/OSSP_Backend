📦 OSSP_BackEnd
├── 🐘 build.gradle                 # 프로젝트 의존성 및 빌드 설정
├── 📂 docs
│   └── 📝 tree.md                  # 문서 디렉토리
├── 📂 gradle/wrapper               # Gradle 래퍼 설정
│   ├── 📦 gradle-wrapper.jar
│   └── ⚙️ gradle-wrapper.properties
├── 🚀 gradlew                      # Mac/Linux용 빌드 스크립트
├── 🚀 gradlew.bat                  # Windows용 빌드 스크립트
├── 📜 LICENSE
├── 📖 README.md
├── ⚙️ settings.gradle              # 프로젝트 모듈 설정
└── 📂 src
├── 📂 main                     # 🚀 실제 서비스 운영 코드
│   ├── 📂 java/com/example/OSSP_BackEnd
│   │   ├── 📂 config           # ⚙️ 서버 전역 및 외부 연동 설정
│   │   │   ├── FcmConfig.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebSocketConfig.java
│   │   ├── 📂 controller       # 🌐 API 엔드포인트 (클라이언트 요청 처리)
│   │   │   ├── AuthController.java
│   │   │   ├── CallRequestController.java
│   │   │   ├── ChatController.java
│   │   │   ├── ReviewController.java
│   │   │   └── UserController.java
│   │   ├── 📂 dto              # 📦 계층 간 데이터 교환 객체 (Request/Response)
│   │   │   ├── ApiResponse.java
│   │   │   ├── 📂 auth
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── TokenRefreshRequest.java
│   │   │   │   └── TokenResponse.java
│   │   │   ├── CallRequestCreateDto.java
│   │   │   ├── CallRequestResponseDto.java
│   │   │   ├── 📂 chat
│   │   │   │   ├── ChatMessageRequest.java
│   │   │   │   ├── ChatMessageResponse.java
│   │   │   │   ├── ChatRoomCreateRequest.java
│   │   │   │   ├── ChatRoomListResponse.java
│   │   │   │   └── ChatRoomResponse.java
│   │   │   ├── DeviceTokenRequest.java
│   │   │   ├── 📂 request
│   │   │   │   ├── DeviceTokenRequestDto.java
│   │   │   │   ├── DutyUpdateRequestDto.java
│   │   │   │   ├── LocationUpdateRequestDto.java
│   │   │   │   ├── RequestAcceptRequestDto.java
│   │   │   │   ├── RequestCreateDto.java
│   │   │   │   └── ReviewCreateRequestDto.java
│   │   │   └── 📂 response
│   │   │       ├── ApiResponse.java
│   │   │       ├── ErrorResponseDto.java
│   │   │       ├── MyProfileResponseDto.java
│   │   │       ├── RequestAcceptDto.java
│   │   │       ├── RequestDetailResponseDto.java
│   │   │       ├── RequestListResponseDto.java
│   │   │       ├── RequestResponseDto.java
│   │   │       ├── ReviewResponseDto.java
│   │   │       ├── UserProfileResponse.java
│   │   │       └── UserProfileResponseDto.java
│   │   ├── 📂 entity           # 🗄️ 데이터베이스 테이블 매핑 도메인
│   │   │   ├── Building.java
│   │   │   ├── CallRequest.java
│   │   │   ├── ChatMessage.java
│   │   │   ├── ChatRoom.java
│   │   │   ├── MatchHistory.java
│   │   │   ├── RequestStatus.java
│   │   │   ├── Role.java
│   │   │   ├── User.java
│   │   │   └── UserReview.java
│   │   ├── 📂 exception        # ⚠️ 전역 예외 처리
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── InvalidRequestStateException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── SelfAcceptNotAllowedException.java
│   │   ├── 🚀 OsspBackEndApplication.java
│   │   ├── 📂 repository       # 💾 데이터베이스 접근 계층 (JPA)
│   │   │   ├── CallRequestRepository.java
│   │   │   ├── ChatMessageRepository.java
│   │   │   ├── ChatRoomRepository.java
│   │   │   ├── MatchHistoryRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── UserReviewRepository.java
│   │   ├── 📂 security         # 🛡️ Spring Security 커스텀 로직
│   │   │   └── CustomUserDetails.java
│   │   └── 📂 service          # 🧠 핵심 비즈니스 로직
│   │       ├── AuthService.java
│   │       ├── CallRequestService.java
│   │       ├── ChatService.java
│   │       ├── CustomUserDetailsService.java
│   │       ├── FcmService.java
│   │       ├── ReviewService.java
│   │       └── UserService.java
│   └── 📂 resources            #  환경 설정 및 정적 파일
│       ├── ⚙️ application.properties
│       ├── ⚙️ application.yml
│       ├── 🔑 firebase-service-key.json
│       └── 📂 static
│           └── 🌐 index.html
└── 📂 test                     # 🧪 테스트 코드 구역
└── 📂 java/com/example/OSSP_BackEnd
├── 📂 controller
│   ├── CallRequestControllerTest.java
│   ├── ReviewControllerTest.java
│   └── UserControllerTest.java
├── OsspBackEndApplicationTests.java
├── 📂 resources
│   └── application-test.yml
└── 📂 service
├── CallRequestServiceIntegrationTest.java
└── UserServiceTest.java