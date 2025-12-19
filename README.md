# API 확장 가이드

## 1. 신규 추가 파일 및 기능 개요
- `src/main/groovy/com/yumyumcoach/domain/stats/**/*`
  - **주간 통계 API**: 로그인한 사용자의 식단/운동 기록을 한국 기준 월요일~일요일 구간으로 집계.
- `src/main/groovy/com/yumyumcoach/domain/aiadvisor/**/*`
  - **Gemini 연동 공용 클라이언트**: `GeminiClient`가 안전하게 프롬프트를 호출하고 유연하게 응답을 파싱.
  - **AI 추천 식단 계획**: 하루 1회 생성 및 저장, 저장된 계획 조회.
  - **AI 챗봇**: 사용자의 건강/주간 통계 정보를 활용해 단발성 Q&A, 모든 대화 저장 및 날짜별 조회.
  - **AI 영양 평가**: 특정 날짜가 속한 주간(해당 날짜까지) 식단 기반으로 부족/적정/과다 평가 저장·조회.
  - **AI 운동 평가**: 특정 날짜가 속한 주간(해당 날짜까지) 운동량 기반으로 부족/적정/과다 및 추천 운동 저장·조회.
- `src/main/resources/mapper/aiadvisor/*.xml`: 위 기능용 MyBatis 매퍼.
- `db/20251220_ai_features.sql`: 신규 AI/통계 기능에 필요한 모든 테이블 생성 스크립트.

## 2. API 스펙 요약 (기능 / Function / API Path / Header / HTTP Method)
| 기능 | Function | API Path | Header | HTTP Method |
| --- | --- | --- | --- | --- |
| 주간 통계 조회 | `getWeeklyStats` | `/api/me/stats/week?date=YYYY-MM-DD` | `Authorization: Bearer {accessToken}` | GET |
| 일일 AI 식단 계획 생성 | `generateDailyPlan` | `/api/me/ai-meal-plans/generate-today` | `Authorization: Bearer {accessToken}` | POST |
| 일일 AI 식단 계획 조회 | `getDailyPlan` | `/api/me/ai-meal-plans/by-date?date=YYYY-MM-DD` | `Authorization: Bearer {accessToken}` | GET |
| AI 챗봇 단발성 질문 | `sendMessage` | `/api/me/ai-chats/send-once` | `Authorization: Bearer {accessToken}` | POST |
| 날짜별 AI 챗봇 기록 조회 | `getDailyHistory` | `/api/me/ai-chats/daily-history?date=YYYY-MM-DD` | `Authorization: Bearer {accessToken}` | GET |
| 주간 영양 평가 생성 | `evaluate` | `/api/me/ai-nutrition-evaluations/run` | `Authorization: Bearer {accessToken}` | POST |
| 주간 영양 평가 조회 | `getEvaluation` | `/api/me/ai-nutrition-evaluations/week-view?date=YYYY-MM-DD` | `Authorization: Bearer {accessToken}` | GET |
| 주간 운동 평가 생성 | `evaluate` | `/api/me/ai-exercise-evaluations/run` | `Authorization: Bearer {accessToken}` | POST |
| 주간 운동 평가 조회 | `getEvaluation` | `/api/me/ai-exercise-evaluations/week-view?date=YYYY-MM-DD` | `Authorization: Bearer {accessToken}` | GET |

## 3. API 별 상세 (Notion 정리용)

### 1) 주간 통계 조회
- **기능**: 특정 날짜가 속한 한국 기준 월요일~일요일 구간의 식단/운동 합계 조회
- **Function**: `getWeeklyStats`
- **API Path**: `/api/me/stats/week?date=YYYY-MM-DD`
- **요청 헤더**
  | 이름 | 값 | 비고 |
  | --- | --- | --- |
  | Authorization | `Bearer {accessToken}` | 필수 |
- **Request Body**: 없음 (`date`는 QueryString, 미입력 시 오늘 기준)
- **Response 200 예시**
```json
{
  "weekStartDate": "2025-12-15",
  "weekEndDate": "2025-12-21",
  "dietStats": [
    {"date": "2025-12-15", "dayOfWeekKorean": "월", "totalCalories": 1230.5, "totalCarbs": 200.1, "totalProtein": 90.0, "totalFat": 35.5},
    {"date": "2025-12-16", "dayOfWeekKorean": "화", "totalCalories": 0.0, "totalCarbs": 0.0, "totalProtein": 0.0, "totalFat": 0.0}
  ],
  "exerciseStats": [
    {"date": "2025-12-15", "dayOfWeekKorean": "월", "totalDurationMinutes": 45.0, "totalCalories": 320.5},
    {"date": "2025-12-16", "dayOfWeekKorean": "화", "totalDurationMinutes": 0.0, "totalCalories": 0.0}
  ]
}
```
- **오류**: `401 AUTH_UNAUTHORIZED` 토큰 오류 시

---

### 2) 일일 AI 식단 계획 생성
- **기능**: 하루 1회 Gemini를 사용해 아침/점심/저녁 식단, 칼로리, 한줄평을 생성 후 저장
- **Function**: `generateDailyPlan`
- **API Path**: `/api/me/ai-meal-plans/generate-today`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**
```json
{ "targetDate": "2025-12-19" }
```
- **Response 201 예시**
```json
{
  "planDate": "2025-12-19",
  "breakfastMenu": "현미밥 + 닭가슴살 + 샐러드",
  "lunchMenu": "연어구이 + 퀴노아 + 브로콜리",
  "dinnerMenu": "닭가슴살 샐러드",
  "breakfastCalories": 400,
  "lunchCalories": 650,
  "dinnerCalories": 450,
  "breakfastNote": "저지방 고단백",
  "lunchNote": "오메가3 풍부",
  "dinnerNote": "저칼로리",
  "totalCalories": 1500,
  "modelUsed": "gemini-pro"
}
```
- **오류**: `400 INVALID_REQUEST` (필수 값 누락)

---

### 3) 일일 AI 식단 계획 조회
- **기능**: 저장된 추천 식단 계획을 날짜별로 조회
- **Function**: `getDailyPlan`
- **API Path**: `/api/me/ai-meal-plans/by-date?date=YYYY-MM-DD`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**: 없음 (QueryString)
- **Response**: 생성 시와 동일 구조

---

### 4) AI 챗봇 단발성 질문
- **기능**: 건강 정보 + 주간 통계 + 질문을 Gemini에 전달해 단발성 답변을 받고, 사용자/AI 메시지를 모두 저장
- **Function**: `sendMessage`
- **API Path**: `/api/me/ai-chats/send-once`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**
```json
{ "conversationDate": "2025-12-19", "message": "오늘 운동 어떻게 할까요?" }
```
- **Response 200 예시** (저장된 메시지 리스트)
```json
[
  {"messageId":1,"conversationDate":"2025-12-19","role":"USER","message":"오늘 운동 어떻게 할까요?","createdAt":"2025-12-19T09:00:00"},
  {"messageId":2,"conversationDate":"2025-12-19","role":"ASSISTANT","message":"주간 운동량을 보면...","createdAt":"2025-12-19T09:00:03"}
]
```

---

### 5) 날짜별 AI 챗봇 기록 조회
- **기능**: 특정 날짜의 사용자/AI 메시지 기록을 시간 순으로 조회
- **Function**: `getDailyHistory`
- **API Path**: `/api/me/ai-chats/daily-history?date=YYYY-MM-DD`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**: 없음
- **Response**: 메시지 배열 (위 예시와 동일 형태)

---

### 6) 주간 영양 평가 생성
- **기능**: 특정 날짜가 속한 주간(해당 날짜까지) 식단을 활용해 탄/단/지/칼로리의 부족/적정/과다 평가 및 분석을 생성·저장
- **Function**: `evaluate`
- **API Path**: `/api/me/ai-nutrition-evaluations/run`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**
```json
{ "targetDate": "2025-12-19" }
```
- **Response 200 예시**
```json
{
  "referenceDate": "2025-12-19",
  "weekStartDate": "2025-12-15",
  "weekEndDate": "2025-12-21",
  "carbohydrateStatus": "적당",
  "proteinStatus": "부족",
  "fatStatus": "적당",
  "calorieStatus": "많음",
  "analysisSummary": "수요일까지 단백질이 낮아 저녁에 보충 권장",
  "modelUsed": "gemini-pro"
}
```

---

### 7) 주간 영양 평가 조회
- **기능**: 저장된 평가를 날짜 기준으로 조회
- **Function**: `getEvaluation`
- **API Path**: `/api/me/ai-nutrition-evaluations/week-view?date=YYYY-MM-DD`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**: 없음
- **Response**: 생성 시와 동일 구조

---

### 8) 주간 운동 평가 생성
- **기능**: 특정 날짜가 속한 주간(해당 날짜까지) 운동량을 평가하고 추천 운동을 제공, 결과 저장
- **Function**: `evaluate`
- **API Path**: `/api/me/ai-exercise-evaluations/run`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**
```json
{ "targetDate": "2025-12-19" }
```
- **Response 200 예시**
```json
{
  "referenceDate": "2025-12-19",
  "weekStartDate": "2025-12-15",
  "weekEndDate": "2025-12-21",
  "volumeStatus": "적당",
  "recommendation": "금요일에 30분 인터벌 러닝 추가를 권장",
  "modelUsed": "gemini-pro"
}
```

---

### 9) 주간 운동 평가 조회
- **기능**: 저장된 주간 운동 평가 조회
- **Function**: `getEvaluation`
- **API Path**: `/api/me/ai-exercise-evaluations/week-view?date=YYYY-MM-DD`
- **요청 헤더**: Authorization Bearer 토큰 필수
- **Request Body**: 없음
- **Response**: 생성 시와 동일 구조

## 4. Postman 테스트 가이드
1. **환경 변수 설정**: `{{baseUrl}}` = `http://localhost:8080`, `{{token}}` = 로그인 후 발급된 Bearer 토큰.
2. **공통 헤더**: `Authorization: Bearer {{token}}`, `Content-Type: application/json` (POST 요청 시).
3. **주간 통계**: GET `{{baseUrl}}/api/me/stats/week?date=2025-12-19` → 200 응답에서 월~일 데이터 확인.
4. **식단 계획 생성**: POST `{{baseUrl}}/api/me/ai-meal-plans/generate-today` with body `{ "targetDate": "2025-12-19" }` → 201 응답 확인 후, GET `/api/me/ai-meal-plans/by-date?date=2025-12-19`로 저장 여부 재검증.
5. **챗봇**: POST `/api/me/ai-chats/send-once` with `{ "conversationDate": "2025-12-19", "message": "오늘 뭐 먹을까요?" }` → 배열 응답에서 USER/ASSISTANT 메시지 순서를 확인. 이어서 GET `/api/me/ai-chats/daily-history?date=2025-12-19`로 기록 조회.
6. **영양 평가**: POST `/api/me/ai-nutrition-evaluations/run` with `{ "targetDate": "2025-12-19" }` → 200 응답 후 GET `/api/me/ai-nutrition-evaluations/week-view?date=2025-12-19`로 재조회.
7. **운동 평가**: POST `/api/me/ai-exercise-evaluations/run` with `{ "targetDate": "2025-12-19" }` → 200 응답 후 GET `/api/me/ai-exercise-evaluations/week-view?date=2025-12-19`로 재조회.

## 5. Gemini API Key 저장 위치
- `backend-develop` 루트에 있는 `.env` 파일 또는 시스템 환경변수에 `gemini.api.key=YOUR_KEY`를 추가합니다.
- `application.yml`은 `.env`를 자동 import 하므로 별도 수정 없이 `GeminiClient`에서 키를 읽어옵니다.

## 6. 신규 테이블 정의 (`db/20251220_ai_features.sql`)
### ai_meal_plans
| 컬럼 | 타입 | 설명 | 제약 |
| --- | --- | --- | --- |
| id | BIGINT | PK | AUTO_INCREMENT |
| email | VARCHAR(255) | 사용자 이메일 | NOT NULL |
| plan_date | DATE | 계획 날짜 (1일 1건) | UNIQUE(email, plan_date) |
| breakfast_menu/lunch_menu/dinner_menu | TEXT | 각 식사 메뉴 | NULL 허용 |
| breakfast_calories/lunch_calories/dinner_calories | DOUBLE | 식사별 칼로리 | NULL 허용 |
| breakfast_note/lunch_note/dinner_note | TEXT | 식사별 한줄평 | NULL 허용 |
| total_calories | DOUBLE | 총 섭취 칼로리 | NULL 허용 |
| model_used | VARCHAR(50) | 사용 모델명 | NULL 허용 |
| created_at | DATETIME | 생성 시각 | 기본값 CURRENT_TIMESTAMP |

### ai_chat_messages
| 컬럼 | 타입 | 설명 | 제약 |
| --- | --- | --- | --- |
| id | BIGINT | PK | AUTO_INCREMENT |
| email | VARCHAR(255) | 사용자 이메일 | NOT NULL, INDEX(email, conversation_date) |
| conversation_date | DATE | 대화 날짜 | NOT NULL |
| role | VARCHAR(20) | `USER` / `ASSISTANT` 구분 | NOT NULL |
| message | TEXT | 메시지 본문 | NULL 허용 |
| created_at | DATETIME | 저장 시각 | 기본값 CURRENT_TIMESTAMP |

### ai_nutrition_evaluations
| 컬럼 | 타입 | 설명 | 제약 |
| --- | --- | --- | --- |
| id | BIGINT | PK | AUTO_INCREMENT |
| email | VARCHAR(255) | 사용자 이메일 | NOT NULL |
| reference_date | DATE | 평가 기준 날짜 | UNIQUE(email, reference_date) |
| week_start_date / week_end_date | DATE | 해당 주간 범위 | NOT NULL |
| carbohydrate_status/protein_status/fat_status/calorie_status | VARCHAR(50) | 영양 성분 상태(부족/적당/많음 등) | NULL 허용 |
| analysis_summary | TEXT | 상세 평가/조언 | NULL 허용 |
| model_used | VARCHAR(50) | 사용 모델명 | NULL 허용 |
| created_at | DATETIME | 생성 시각 | 기본값 CURRENT_TIMESTAMP |

### ai_exercise_evaluations
| 컬럼 | 타입 | 설명 | 제약 |
| --- | --- | --- | --- |
| id | BIGINT | PK | AUTO_INCREMENT |
| email | VARCHAR(255) | 사용자 이메일 | NOT NULL |
| reference_date | DATE | 평가 기준 날짜 | UNIQUE(email, reference_date) |
| week_start_date / week_end_date | DATE | 해당 주간 범위 | NOT NULL |
| volume_status | VARCHAR(50) | 운동량 평가(부족/적당/많음) | NULL 허용 |
| recommendation | TEXT | 추천 운동 및 코멘트 | NULL 허용 |
| model_used | VARCHAR(50) | 사용 모델명 | NULL 허용 |
| created_at | DATETIME | 생성 시각 | 기본값 CURRENT_TIMESTAMP |
