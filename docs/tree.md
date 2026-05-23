.
├── ⚙️ 프로젝트 설정 및 빌드
│   ├── build.gradle                 # 프로젝트 의존성(라이브러리) 및 빌드 설정
│   ├── settings.gradle              # 프로젝트 이름 및 모듈 설정
│   ├── gradlew / gradlew.bat        # Gradle 래퍼 실행 스크립트
│   └── gradle/                      # Gradle 래퍼 관련 파일
├── 📄 문서
│   ├── [README.md](http://readme.md/)                    # 프로젝트 설명 문서
│   └── LICENSE                      # 라이선스 정보
└── 📁 src (소스 코드 계층)
├── 📂 main
│   ├── ☕ java/com/example/OSSP_BackEnd
│   │   ├── 🔧 config/           # 서버 전역 설정 (FcmConfig, WebSocketConfig)
│   │   ├── 🌐 controller/       # 클라이언트 요청을 받는 API 엔드포인트
│   │   ├── 📦 dto/              # 계층 간 데이터 교환을 위한 객체 (Request/Response)
│   │   ├── 🗄️ entity/           # 데이터베이스 테이블과 매핑되는 도메인 모델
│   │   ├── ⚠️ exception/        # 전역 예외 처리 및 커스텀 에러 정의
│   │   ├── 💾 repository/       # 데이터베이스 접근을 담당하는 인터페이스
│   │   ├── ⚙️ service/          # 핵심 비즈니스 로직 처리 구역
│   │   └── 🚀 OsspBackEndApplication.java # Spring Boot 애플리케이션 실행 엔트리포인트
│   └── 📁 resources
│       ├── application.properties    # 서버 포트, DB 연결 등 핵심 환경 변수 설정
│       ├── firebase-service-key.json # 외부 서비스(FCM) 인증 키
│       └── static/index.html         # 정적 리소스 파일
└── 📂 test (테스트 코드 계층)
├── ☕ java/com/example/OSSP_BackEnd
│   ├── controller/               # API 컨트롤러 테스트
│   ├── service/                  # 비즈니스 로직 테스트
│   └── OsspBackEndApplicationTests.java
└── 📁 resources
└── application-test.yml      # 테스트 전용 환경 설정 파일