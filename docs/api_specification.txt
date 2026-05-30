# OSSP Back-End API 명세서

## 📌 공통 안내사항

### 응답 형식

모든 API 응답은 일관된 형식의 JSON 객체로 제공됩니다.

- **성공 시**: `data` 필드에 실제 응답 데이터가 포함됩니다.
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "요청이 성공적으로 처리되었습니다.",
    "data": { ... } 
  }
  ```
- **실패 시**: `data` 필드는 `null`이며, `message` 필드에 실패 원인이 명시됩니다.
  ```json
  {
    "status": "ERROR",
    "statusCode": 400,
    "message": "요청이 올바르지 않습니다.",
    "data": null
  }
  ```

---

## 🔐 1. 인증 (Authentication)

### 1.1. 회원가입

새로운 사용자를 등록합니다. 이메일은 `@dgu.ac.kr` 도메인만 허용됩니다.

- **Endpoint**: `POST /api/v1/auth/signup`
- **Request Body**:
  ```json
  {
    "email": "student@dgu.ac.kr",
    "password": "password123",
    "nickname": "동국이"
  }
  ```
- **Success Response**: `201 Created` 상태 코드와 함께 Body는 비어있습니다.
- **Failure Response**:
  - `400 Bad Request`: 이미 사용 중인 이메일일 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 400,
      "message": "이미 사용 중인 이메일입니다.",
      "data": null
    }
    ```

### 1.2. 로그인

사용자 이메일과 비밀번호로 로그인하여 API 접근 토큰을 발급받습니다.

- **Endpoint**: `POST /api/v1/auth/login`
- **Request Body**:
  ```json
  {
    "email": "user@dgu.ac.kr",
    "password": "password123"
  }
  ```
- **Success Response Body**:
  ```json
  {
    "grantType": "Bearer",
    "accessToken": "...",
    "refreshToken": "..."
  }
  ```
- **Failure Response**:
  - `401 Unauthorized`: 이메일이 존재하지 않거나 비밀번호가 틀렸을 경우 (Body 없음)

### 1.3. 토큰 재발급

기존에 발급받은 Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

- **Endpoint**: `POST /api/v1/auth/refresh`
- **Request Body**:
  ```json
  {
    "refreshToken": "your_refresh_token"
  }
  ```
- **Success Response Body**:
  ```json
  {
    "grantType": "Bearer",
    "accessToken": "...",
    "refreshToken": "..."
  }
  ```
- **Failure Response**:
  - `400 Bad Request`: Refresh Token이 유효하지 않거나 만료되었을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 400,
      "message": "유효하지 않은 리프레시 토큰입니다.",
      "data": null
    }
    ```

---

## 👤 2. 사용자 (User)

### 2.1. 내 프로필 조회

현재 로그인된 사용자의 상세 정보를 조회합니다.

- **Endpoint**: `GET /api/v1/users/me`
- **Success Response Body**:
  ```json
  {
    "userId": 1,
    "nickname": "동국이",
    "email": "student@dgu.ac.kr",
    "mannerScore": 3.65,
    "currentBuilding": "신공학관",
    "isOnDuty": true,
    "role": "USER",
    "createdAt": "2023-10-27T10:00:00"
  }
  ```
- **Failure Response**:
  - `404 Not Found`: 사용자 정보를 찾을 수 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "사용자를 찾을 수 없습니다.",
      "data": null
    }
    ```

### 2.2. 특정 사용자 프로필 조회

사용자 ID를 통해 특정 사용자의 공개 프로필 정보를 조회합니다.

- **Endpoint**: `GET /api/v1/users/{userId}`
- **Success Response Body**:
  ```json
  {
    "userId": 2,
    "nickname": "코끼리",
    "mannerScore": 4.0
  }
  ```
- **Failure Response**:
  - `404 Not Found`: `userId`에 해당하는 사용자가 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "사용자를 찾을 수 없습니다.",
      "data": null
    }
    ```

### 2.3. 위치 정보 업데이트

사용자의 현재 위치(건물)를 업데이트합니다.

- **Endpoint**: `PATCH /api/v1/users/location`
- **Request Body**:
  ```json
  {
    "currentBuilding": "정보과학관"
  }
  ```
- **Success Response**: `200 OK`와 함께 성공 메시지를 반환합니다.
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "위치 정보가 성공적으로 갱신되었습니다.",
    "data": null
  }
  ```
- **Failure Response**:
  - `400 Bad Request`: 요청 본문이 유효하지 않을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 400,
      "message": "건물 이름은 비어 있을 수 없습니다.",
      "data": null
    }
    ```
  - `404 Not Found`: 사용자 정보를 찾을 수 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "사용자를 찾을 수 없습니다.",
      "data": null
    }
    ```

### 2.4. 당직 상태 업데이트

실시간 요청 알림을 받을지 여부(당직 상태)를 설정합니다.

- **Endpoint**: `PATCH /api/v1/users/me/duty`
- **Request Body**:
  ```json
  {
    "isOnDuty": true
  }
  ```
- **Success Response**: `200 OK`와 함께 성공 메시지를 반환합니다.
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "알림 받기 설정이 성공적으로 변경되었습니다.",
    "data": null
  }
  ```
- **Failure Response**:
  - `404 Not Found`: 사용자 정보를 찾을 수 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "사용자를 찾을 수 없습니다.",
      "data": null
    }
    ```

### 2.5. 디바이스 토큰 등록

푸시 알림을 위한 FCM 디바이스 토큰을 서버에 등록합니다.

- **Endpoint**: `PATCH /api/v1/users/me/device-token`
- **Request Body**:
  ```json
  {
    "fcmToken": "your_fcm_device_token"
  }
  ```
- **Success Response**: `200 OK`와 함께 성공 메시지를 반환합니다.
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "기기 토큰이 성공적으로 등록되었습니다.",
    "data": null
  }
  ```
- **Failure Response**:
  - `400 Bad Request`: FCM 토큰이 유효하지 않을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 400,
      "message": "FCM 토큰은 비어 있을 수 없습니다.",
      "data": null
    }
    ```
  - `404 Not Found`: 사용자 정보를 찾을 수 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "사용자를 찾을 수 없습니다.",
      "data": null
    }
    ```

### 2.6. 자신이 받은 리뷰 목록 조회

현재 로그인된 사용자가 다른 사용자로부터 받은 모든 리뷰를 조회합니다. (마이페이지용)

- **Endpoint**: `GET /api/v1/users/me/reviews`
- **Success Response Body**:
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "내가 받은 리뷰 목록을 성공적으로 조회했습니다.",
    "data": [
      {
        "reviewId": 1,
        "matchId": 1,
        "reviewerNickname": "코끼리",
        "score": 5.0,
        "comments": "친절하고 좋았어요!",
        "createdAt": "2023-10-27T10:00:00"
      }
    ]
  }
  ```
- **Failure Response**:
  - `401 Unauthorized`: 인증되지 않은 사용자의 요청일 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 401,
      "message": "인증 정보가 유효하지 않습니다.",
      "data": null
    }
    ```

### 2.7. 특정 사용자가 받은 리뷰 목록 조회

특정 사용자가 다른 사용자로부터 받은 모든 리뷰를 조회합니다. (타인 프로필 조회용)

- **Endpoint**: `GET /api/v1/users/{userId}/reviews`
- **Success Response Body**:
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "유저가 받은 리뷰 목록을 성공적으로 조회했습니다.",
    "data": [
      {
        "reviewId": 2,
        "matchId": 2,
        "reviewerNickname": "동국이",
        "score": 4.5,
        "comments": "시간 약속을 잘 지켜요.",
        "createdAt": "2023-10-28T11:00:00"
      }
    ]
  }
  ```
- **Failure Response**:
  - `404 Not Found`: `userId`에 해당하는 사용자가 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "사용자를 찾을 수 없습니다.",
      "data": null
    }
    ```

---

## 📦 3. 대여 요청 (Call Request)

### 대여 요청 상태 (RequestStatus)
- `WAITING`: 대기 중
- `MATCHED`: 매칭 완료
- `CANCELED`: 요청 취소
- `IN_USE`: 사용 중 (물품 전달 완료)
- `COMPLETED`: 거래 완료

### 3.1. 대여 요청 생성

- **Endpoint**: `POST /api/v1/requests`
- **Request Body**:
  ```json
  {
    "itemName": "아이폰 충전기",
    "buildingName": "정보과학관",
    "rewardAmt": 1000,
    "duration": 60,
    "memo": "C타입 충전기 빌려주실 분 구합니다!"
  }
  ```
- **Success Response Body**:
  ```json
  {
    "requestId": 1,
    "itemName": "아이폰 충전기",
    "buildingName": "정보과학관",
    "rewardAmt": 1000,
    "duration": 60,
    "memo": "C타입 충전기 빌려주실 분 구합니다!",
    "status": "WAITING",
    "createdAt": "2023-10-27T10:00:00",
    "requesterNickname": "동국이"
  }
  ```
- **Failure Response**:
  - `400 Bad Request`: 요청 본문 데이터가 유효하지 않을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 400,
      "message": "물품 이름은 비어 있을 수 없습니다.",
      "data": null
    }
    ```
  - `401 Unauthorized`: 인증되지 않은 사용자의 요청일 경우 (Body 없음)

### 3.2. 대여 요청 목록 조회

- **Endpoint**: `GET /api/v1/requests`
- **Query Parameter**: `status` (String, optional) - 조회할 요청 상태 (위에 명시된 `RequestStatus` 값 중 하나). 생략 시 모든 상태 조회.
- **Success Response Body**: `List<Object>`
  - **`WAITING`, `CANCELED` 상태일 경우**:
    ```json
    [
      {
        "requestId": 1,
        "itemName": "아이폰 충전기",
        "buildingName": "정보과학관",
        "rewardAmt": 1000,
        "status": "WAITING",
        "createdAt": "2023-10-27T10:00:00",
        "requesterNickname": "동국이",
        "requesterId": 1
      }
    ]
    ```
  - **`MATCHED`, `IN_USE`, `COMPLETED` 상태일 경우**:
    ```json
    [
      {
        "requestId": 1,
        "itemName": "아이폰 충전기",
        "buildingName": "정보과학관",
        "rewardAmt": 1000,
        "status": "MATCHED",
        "createdAt": "2023-10-27T10:00:00",
        "requesterNickname": "동국이",
        "requesterId": 1,
        "matchId": 1,
        "providerId": 2
      }
    ]
    ```
- **Failure Response**:
  - `400 Bad Request`: `status` 쿼리 파라미터 값이 유효하지 않은 `RequestStatus` 열거형 값일 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 400,
      "message": "잘못된 요청 상태 값입니다.",
      "data": null
    }
    ```

### 3.3. 내 대여 요청 목록 조회

- **Endpoint**: `GET /api/v1/requests/me`
- **Query Parameter**: `status` (String, optional) - 조회할 요청 상태 (위에 명시된 `RequestStatus` 값 중 하나). 생략 시 모든 상태 조회.
- **Success Response Body**: `List<Object>`
  - **`WAITING`, `CANCELED` 상태일 경우**:
    ```json
    [
      {
        "requestId": 1,
        "itemName": "아이폰 충전기",
        "buildingName": "정보과학관",
        "rewardAmt": 1000,
        "status": "WAITING",
        "createdAt": "2023-10-27T10:00:00",
        "requesterNickname": "동국이",
        "requesterId": 1
      }
    ]
    ```
  - **`MATCHED`, `IN_USE`, `COMPLETED` 상태일 경우**:
    ```json
    [
      {
        "requestId": 1,
        "itemName": "아이폰 충전기",
        "buildingName": "정보과학관",
        "rewardAmt": 1000,
        "status": "MATCHED",
        "createdAt": "2023-10-27T10:00:00",
        "requesterNickname": "동국이",
        "requesterId": 1,
        "matchId": 1,
        "providerId": 2
      }
    ]
    ```
- **Failure Response**:
  - `401 Unauthorized`: 인증되지 않은 사용자의 요청일 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 401,
      "message": "인증 정보가 유효하지 않습니다.",
      "data": null
    }
    ```

### 3.4. 주변 대여 요청 목록 조회

- **Endpoint**: `GET /api/v1/requests/nearby`
- **Description**: 사용자의 현재 위치를 기준으로 주변의 대여 요청 목록을 조회합니다. 오직 `WAITING` 상태인 요청만 조회합니다.
- **Success Response Body**: `List<Object>` (3.2. 응답 형식과 동일)
- **Failure Response**:
  - `401 Unauthorized`: 인증되지 않은 사용자의 요청일 경우 (Body 없음)

### 3.5. 대여 요청 상세 조회

- **Endpoint**: `GET /api/v1/requests/{requestId}`
- **Success Response Body**:
  ```json
  {
    "requestId": 1,
    "itemName": "아이폰 충전기",
    "buildingName": "정보과학관",
    "rewardAmt": 1000,
    "duration": 60,
    "memo": "C타입 충전기 빌려주실 분 구합니다!",
    "status": "WAITING",
    "createdAt": "2023-10-27T10:00:00",
    "requesterId": 1,
    "requesterNickname": "동국이"
  }
  ```
- **Failure Response**:
  - `404 Not Found`: `requestId`에 해당하는 요청이 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "해당 ID의 대여 요청을 찾을 수 없습니다.",
      "data": null
    }
    ```

### 3.6. 대여 요청 수락

- **Endpoint**: `POST /api/v1/requests/{requestId}/accept`
- **Request Body**:
  ```json
  {
    "providerId": 2 // 수락하는 사용자의 ID
  }
  ```
- **Success Response Body**:
  ```json
  {
    "matchId": 1,
    "requestId": 1,
    "requestStatus": "MATCHED",
    "requesterId": 1,
    "providerId": 2,
    "providerNickname": "코끼리",
    "matchedAt": "2023-10-27T10:05:00"
  }
  ```
- **Failure Response**:
  - `404 Not Found`: `requestId`에 해당하는 요청이 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "해당 ID의 대여 요청을 찾을 수 없습니다.",
      "data": null
    }
    ```
  - `409 Conflict`: 요청을 수락할 수 없는 경우 (자신의 요청, 이미 처리된 요청 등)
    ```json
    {
      "status": "ERROR",
      "statusCode": 409,
      "message": "자신이 올린 요청은 수락할 수 없습니다.",
      "data": null
    }
    ```
    ```json
    {
      "status": "ERROR",
      "statusCode": 409,
      "message": "이미 처리된 대여 요청입니다.",
      "data": null
    }
    ```

### 3.7. 대여 요청 상태 변경

- **요청 취소**: `PATCH /api/v1/requests/{requestId}/cancel`
- **물품 전달 완료**: `PATCH /api/v1/requests/{requestId}/handover`
- **거래 완료**: `PATCH /api/v1/requests/{requestId}/complete`
- **Success Response**: 성공 시 `200 OK`와 함께 성공 메시지를 반환합니다.
- **Failure Response**:
  - `404 Not Found`: `requestId`에 해당하는 요청이 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "해당 ID의 대여 요청을 찾을 수 없습니다.",
      "data": null
    }
    ```
  - `409 Conflict`: 상태를 변경할 수 없는 경우 (권한이 없거나, 상태 변경 규칙 위배)
    ```json
    {
      "status": "ERROR",
      "statusCode": 409,
      "message": "요청을 취소할 권한이 없습니다.",
      "data": null
    }
    ```
    ```json
    {
      "status": "ERROR",
      "statusCode": 409,
      "message": "대기 중인 요청만 취소할 수 있습니다.",
      "data": null
    }
    ```

---

## 💬 4. 채팅 (Chat)

### 4.1. 채팅방 생성

- **Endpoint**: `POST /api/v1/chats`
- **Request Body**:
  ```json
  {
    "matchId": 1 // 매칭 ID
  }
  ```
- **Response Body**:
  ```json
  {
    "roomId": 1,
    "matchId": 1,
    "createdAt": "2023-10-27T10:05:00"
  }
  ```

### 4.2. 채팅방 목록 조회

- **Endpoint**: `GET /api/v1/chats`
- **Response Body**: `List<Object>`
  ```json
  [
    {
      "requestId": 1,
      "matchId": 1,
      "roomId": 1,
      "opponentId": 2,
      "opponentName": "코끼리",
      "lastMessage": "안녕하세요!",
      "updatedAt": "2023-10-27T10:06:00"
    }
  ]
  ```

### 4.3. 이전 채팅 메시지 조회

- **Endpoint**: `GET /api/v1/chats/{roomId}/messages`
- **Response Body**: `List<Object>`
  ```json
  [
    {
      "senderId": 1,
      "content": "안녕하세요!",
      "createdAt": "2023-10-27T10:06:00"
    }
  ]
  ```

### 4.4. 실시간 채팅 (WebSocket/STOMP)

#### 4.4.1. STOMP 연결

- **Endpoint**: `ws://<your-server-address>/ws-stomp`
- **Description**: 클라이언트는 이 주소로 WebSocket 연결을 시작하여 STOMP 세션을 맺습니다. 헤더에 특별한 인증 정보는 필요하지 않습니다.

#### 4.4.2. 채팅방 구독 (Subscribe)

- **Destination**: `/topic/rooms/{roomId}`
- **Description**: 특정 채팅방(`roomId`)의 메시지를 수신하기 위해 이 경로를 구독합니다. 구독이 성공하면 해당 채팅방에 새로운 메시지가 도착할 때마다 클라이언트로 메시지가 전송됩니다.
- **수신 메시지 형식**:
  ```json
  {
    "senderId": 2,
    "content": "네, 안녕하세요!",
    "createdAt": "2023-10-27T10:07:00"
  }
  ```

#### 4.4.3. 메시지 전송 (Publish)

- **Destination**: `/app/chat.send`
- **Message Body**:
  ```json
  {
    "roomId": 1,
    "senderId": 1,
    "content": "네, 안녕하세요!"
  }
  ```
- **Description**: 클라이언트가 채팅 메시지를 서버로 보낼 때 사용합니다. 서버는 이 메시지를 받아 해당 `roomId`를 구독하고 있는 모든 클라이언트에게 브로드캐스트합니다.

#### 4.4.4. 구독 해제 (Unsubscribe)

- **Description**: 클라이언트가 특정 채팅방의 메시지 수신을 중단하고 싶을 때, STOMP 클라이언트 라이브러리에서 제공하는 `unsubscribe` 기능을 사용하여 기존 구독을 해제합니다. 별도의 서버 API 호출은 필요하지 않습니다.

---

## ⭐ 5. 리뷰 (Review)

### 5.1. 리뷰 작성

- **Endpoint**: `POST /api/v1/reviews`
- **Request Body**:
  ```json
  {
    "matchId": 1,
    "score": 5.0,
    "comments": "친절하고 좋았어요!"
  }
  ```
- **Success Response**: `200 OK`와 함께 성공 메시지를 반환합니다.
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "리뷰가 성공적으로 등록되었습니다.",
    "data": null
  }
  ```
- **Failure Response**:
  - `404 Not Found`: `matchId`에 해당하는 매칭 기록이 없을 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 404,
      "message": "매칭 기록을 찾을 수 없습니다.",
      "data": null
    }
    ```
  - `409 Conflict`: 거래가 완료되지 않았거나, 이미 리뷰를 작성했거나, 거래 당사자가 아닌 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 409,
      "message": "거래가 완료되지 않은 요청에 대한 리뷰는 작성할 수 없습니다.",
      "data": null
    }
    ```
    ```json
    {
      "status": "ERROR",
      "statusCode": 409,
      "message": "이미 이 거래에 대한 리뷰를 작성했습니다.",
      "data": null
    }
    ```
    ```json
    {
      "status": "ERROR",
      "statusCode": 409,
      "message": "해당 거래의 당사자만 리뷰를 작성할 수 있습니다.",
      "data": null
    }
    ```

### 5.2. 자신이 작성한 리뷰 목록 조회

현재 로그인된 사용자가 다른 사용자에게 작성한 모든 리뷰를 조회합니다.

- **Endpoint**: `GET /api/v1/reviews/my`
- **Success Response Body**:
  ```json
  {
    "status": "OK",
    "statusCode": 200,
    "message": "자신이 작성한 리뷰 목록입니다.",
    "data": [
      {
        "reviewId": 1,
        "matchId": 1,
        "revieweeNickname": "코끼리",
        "score": 5.0,
        "comments": "친절하고 좋았어요!",
        "createdAt": "2023-10-27T10:00:00"
      }
    ]
  }
  ```
- **Failure Response**:
  - `401 Unauthorized`: 인증되지 않은 사용자의 요청일 경우
    ```json
    {
      "status": "ERROR",
      "statusCode": 401,
      "message": "인증 정보가 유효하지 않습니다.",
      "data": null
    }
    ```