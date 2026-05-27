# OSSP 매칭 알고리즘 고도화 구현 문서

## 📋 개요
물건 대여 매칭 플랫폼의 고도화된 매칭 알고리즘을 구현한 Spring Boot 백엔드 로직입니다.

## 🎯 구현된 핵심 기능

### 1. 가중치 기반 타겟팅 시스템
- **매칭 파워 스코어**: 0~100점 만점으로 공급자의 적합도 평가
- **평가 지표**:
  - 매너 점수 (50%): 5.0 만점 기준 환산
  - 최근 7일 대여 횟수 (30%): 활동성 평가
  - 물건 대여 이력 (20%): 유사 물품 제공 경험

### 2. 신규 유저 쉴드 (Cold Start 방어)
- **조건**: 공급 이력 < 3회 AND 가입 후 24시간 이내
- **효과**: 가중치 점수 50점 미만이어도 일반 타겟으로 취급

### 3. 단계별 매칭 프로세스

#### Phase 1 (즉시 실행)
- **일반 타겟**: 동일 건물 + (가중치 >= 50 OR 신규 쉴드)
- **정예 타겟**: 인접 1차 구역 + 가중치 >= 80

#### Phase 2 (5분 후 실행)
- **일반 타겟**: 인접 1차 구역 + (가중치 >= 50 OR 신규 쉴드)
- **정예 타겟**: 인접 2차 구역 + 가중치 >= 80

#### Phase 3 (15분 후 Fallback)
- 수요자에게 보상금 인상 제안 넛지 알림

### 4. 도배 방지
- 수요자는 동시에 단 1건의 WAITING 상태 요청만 가능
- 중복 요청 시 `DuplicateWaitingRequestException` 발생

### 5. 잠수 유저 차단 (배치)
- **실행 시간**: 매일 새벽 4시
- **조건**: `last_active_at`이 5일 이전
- **액션**: `is_on_duty` 자동 OFF 처리

## 📁 구현 파일 구조

```
src/main/java/com/example/OSSP_BackEnd/
├── entity/
│   └── User.java                          // lastActiveAt 필드 추가
├── repository/
│   ├── UserRepository.java                 // 활성 유저 조회, 잠수 유저 조회
│   ├── MatchHistoryRepository.java         // 가중치 계산용 쿼리
│   └── CallRequestRepository.java          // 도배 방지 쿼리
├── service/
│   ├── MatchingScoreService.java           // 가중치 계산 로직
│   ├── MatchingAlgorithmService.java       // Phase 1/2/3 매칭 로직
│   └── CallRequestService.java             // 도배 방지 + Phase 1 트리거
├── scheduler/
│   └── MatchingScheduler.java              // Phase 2/3 + 잠수 유저 차단
└── exception/
    └── DuplicateWaitingRequestException.java
```

## 🔧 설정 및 활성화

### Application 설정
```java
@EnableJpaAuditing
@EnableScheduling  // ✅ 스케줄러 활성화
@EnableAsync      // ✅ 비동기 처리 활성화
@SpringBootApplication
public class OsspBackEndApplication { }
```

### DB 스키마 변경 사항
```sql
-- User 테이블에 컬럼 추가
ALTER TABLE users 
ADD COLUMN last_active_at DATETIME;

-- 기존 레코드에 기본값 설정 (선택)
UPDATE users 
SET last_active_at = created_at 
WHERE last_active_at IS NULL;
```

## 🎮 사용 흐름

### 1. 대여 요청 생성
```java
// CallRequestController를 통해 호출
POST /api/requests
{
  "itemName": "충전기",
  "buildingName": "INFO_CULTURE",
  "rewardAmt": 2000,
  "duration": 30,
  "memo": "C타입 충전기 부탁드립니다"
}

// 자동 실행:
// 1. 도배 방지 체크 ✅
// 2. DB 저장
// 3. Phase 1 매칭 비동기 실행 ✅
```

### 2. 자동 매칭 프로세스
```
생성 즉시  → Phase 1 (동일 건물 + 인접 1차 정예)
   ↓ 5분 대기
생성 후 5분 → Phase 2 (인접 1차 일반 + 인접 2차 정예)
   ↓ 10분 대기
생성 후 15분 → Phase 3 (수요자에게 넛지 알림)
```

### 3. 스케줄러 자동 실행
```
매 1분마다        → Phase 2/3 타이밍 체크 및 실행
매일 새벽 4시      → 잠수 유저 차단
매 시간 30분       → 오래된 WAITING 요청 자동 취소
```

## 🔍 핵심 로직 상세

### 가중치 계산 예시
```java
User user = ...;
String itemName = "충전기";

// 매너 점수: 4.5 → 90점 (50% 가중치 = 45점)
// 최근 활동: 3회 → 60점 (30% 가중치 = 18점)
// 물품 이력: 있음 → 100점 (20% 가중치 = 20점)
// 총점: 83점

double score = matchingScoreService.calculateMatchingScore(user, itemName);
// score = 83.0
```

### 신규 유저 쉴드 판별
```java
// 가입 후 23시간 경과, 공급 이력 2회
boolean hasShield = matchingScoreService.isNewUserWithShield(user);
// hasShield = true

// 가중치가 30점이어도 일반 타겟 자격 획득!
```

### 인접 건물 BFS 탐색
```java
// 1차 인접 건물 조회
List<String> level1 = getAdjacentBuildings("INFO_CULTURE", 1);
// ["WONHEUNG", "MANHAE_PLAZA", "HAKRIM"]

// 2차 인접 건물 조회 (1차 인접 건물들의 인접 건물)
List<String> level2 = getAdjacentBuildings("INFO_CULTURE", 2);
// ["GEUMGANG", "MAIN_BUILDING", ...]
```

## ⚠️ 주의사항

### 1. 인접 건물 관계 수정 필요
`MatchingAlgorithmService.java`의 `ADJACENT_BUILDINGS` Map을 실제 캠퍼스 지도에 맞게 수정하세요.

```java
private static final Map<String, List<String>> ADJACENT_BUILDINGS = new HashMap<>() {{
    put("INFO_CULTURE", Arrays.asList("WONHEUNG", "MANHAE_PLAZA", "HAKRIM"));
    // ... 실제 인접 관계로 업데이트
}};
```

### 2. Phase 2/3 대기 시간 조정
```java
private static final int PHASE2_DELAY_MINUTES = 5;   // 기본 5분
private static final int PHASE3_DELAY_MINUTES = 15;  // 기본 15분
```

### 3. 가중치 컷오프 점수 조정
```java
private static final double GENERAL_TARGET_CUTOFF = 50.0;  // 일반 타겟
private static final double ELITE_TARGET_CUTOFF = 80.0;    // 정예 타겟
```

## 🧪 테스트 권장 사항

### 1. 단위 테스트
```java
@Test
void 가중치_계산_테스트() {
    User user = createTestUser(4.5, 3);
    double score = matchingScoreService.calculateMatchingScore(user, "충전기");
    assertThat(score).isGreaterThanOrEqualTo(50.0);
}
```

### 2. 통합 테스트
```java
@Test
void Phase1_매칭_타겟_선정_테스트() {
    CallRequest request = createTestRequest("INFO_CULTURE", "충전기");
    matchingAlgorithmService.executePhase1Matching(request);
    
    // FCM 발송 로그 확인
    verify(fcmService, atLeast(1)).sendMessageTo(any(), any(), any());
}
```

### 3. 스케줄러 테스트
```java
@Test
void 잠수유저_차단_스케줄러_테스트() {
    User inactiveUser = createInactiveUser(6); // 6일 전 활동
    matchingScheduler.deactivateInactiveUsers();
    
    User updated = userRepository.findById(inactiveUser.getId()).get();
    assertThat(updated.getIsOnDuty()).isFalse();
}
```

## 📊 성능 최적화

### 1. 쿼리 최적화
- **N+1 문제 방지**: `JOIN FETCH` 사용
- **인덱스 추가 권장**:
  ```sql
  CREATE INDEX idx_user_on_duty ON users(is_on_duty, current_building);
  CREATE INDEX idx_match_history_provider ON match_history(provider_id, matched_at);
  CREATE INDEX idx_call_request_status ON call_requests(status, created_at);
  ```

### 2. 비동기 처리
- Phase 1 매칭은 `@Async`로 비동기 실행
- API 응답 시간에 영향 없음

### 3. 배치 처리
- FCM 알림은 실패해도 전체 프로세스 중단 없음
- 예외 처리 및 로깅으로 안전성 확보

## 🎉 구현 완료 체크리스트

- [x] User 엔티티 `lastActiveAt` 필드 추가
- [x] Repository 쿼리 메서드 확장
- [x] 가중치 계산 서비스 구현
- [x] Phase 1/2/3 매칭 알고리즘 구현
- [x] 스케줄러 구현 (Phase 2/3, 잠수 유저 차단)
- [x] 도배 방지 로직 구현
- [x] @EnableScheduling, @EnableAsync 활성화
- [x] 예외 처리 및 로깅
- [x] 상세 주석 작성

## 📝 후속 작업 권장사항

1. **실제 건물 관계 매핑**: 캠퍼스 지도 기반 ADJACENT_BUILDINGS 업데이트
2. **FCM 페이로드 커스터마이징**: 알림 클릭 시 상세 화면 이동 구현
3. **매칭 성공률 모니터링**: Phase별 매칭 성공률 통계 수집
4. **A/B 테스트**: 가중치 비율 및 컷오프 점수 최적화
5. **lastActiveAt 자동 업데이트**: 사용자 활동 시 자동 갱신 로직 추가

---

**작성자**: AI Assistant  
**작성일**: 2026.05.24  
**버전**: 1.0.0
