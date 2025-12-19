# YumYumCoach 확장 기능 요약

본 PR에서는 기존 코드를 변경하지 않고 신규 API와 DB 테이블을 추가하여 통계/AI 추천 기능을 제공합니다. 모든 API는 로그인한 사용자를 기준으로 동작하며, 기존 `CurrentUser` 헬퍼를 그대로 활용합니다.

## 1. 추가된 파일과 기능 개요

| 영역 | 추가 파일/경로 | 주요 내용 |
| --- | --- | --- |
| 통계 API | `domain/stats/controller/WeeklyStatsController.java`<br>`domain/stats/service/WeeklyStatsService.java`<br>`domain/stats/mapper/WeeklyStatsMapper.java`<br>`resources/mapper/stats/WeeklyStatsMapper.xml` | 주어진 날짜가 속한 주(월~일)의 식단/운동 합계를 조회. 요일은 한국어 기준, 데이터가 없을 때도 0으로 채워 그래프용 시계열을 보장. |
| AI 클라이언트 | `domain/ai/client/GeminiClient.java` | Gemini 호출 래퍼. API 키가 없거나 오류 시 안전한 문자열을 반환. |
| AI 식단 계획 | `domain/ai/service/AiMealPlanService.java`<br>`domain/ai/controller/AiController.java`<br>`domain/ai/dto/*`<br>`domain/ai/mapper/AiMealPlanMapper.java`<br>`resources/mapper/ai/AiMealPlanMapper.xml` | 하루 1회 로그인 사용자의 건강정보/주간 통계 기반 식단 추천을 Gemini로 생성하고 DB에 저장/조회. |
| AI 챗봇 | `domain/ai/service/AiChatService.java`<br>`domain/ai/controller/AiController.java`<br>`domain/ai/mapper/AiChatMessageMapper.java`<br>`resources/mapper/ai/AiChatMessageMapper.xml` | 질문마다 건강 정보와 주간 통계를 첨부해 Gemini에게 전달하고, 사용자/봇 메시지를 날짜별로 저장·조회. |
| AI 주간 평가 | `domain/ai/service/AiWeeklyReviewService.java`<br>`domain/ai/controller/AiController.java`<br>`domain/ai/mapper/AiWeeklyReviewMapper.java`<br>`resources/mapper/ai/AiWeeklyReviewMapper.xml` | 주간 식단/운동 데이터를 오늘까지만 반영해 영양/운동 평가 및 추천을 생성·저장. |
| DB 스키마 | `db/20250205_ai_features.sql` | AI 식단 계획, 챗봇 로그, 주간 영양 평가, 주간 운동 평가 테이블 생성 쿼리. |
| 응답 DTO | `domain/stats/dto/WeeklyStatsResponse.java` 등 | 프론트 그래프용 시계열과 AI 응답 전달용 DTO 정의. |

## 2. API 요약 (기능 · Function · Path · Header · Method)

| 기능 | Function | API Path | Header | HTTP Method |
| --- | --- | --- | --- | --- |
| 주간 통계 조회 | `getWeeklyStats` | `/api/me/stats/week?date=YYYY-MM-DD` | `Authorization: Bearer {accessToken}` | GET |
| AI 식단 계획 생성/조회 | `generateDailyPlan` | `/api/me/ai/meal-plans` | `Authorization: Bearer {accessToken}` | POST |
| AI 챗봇 질문 | `ask` | `/api/me/ai/chats` | `Authorization: Bearer {accessToken}` | POST |
| AI 챗봇 히스토리 조회 | `getHistory` | `/api/me/ai/chats?date=YYYY-MM-DD` | `Authorization: Bearer {accessToken}` | GET |
| 주간 영양 평가 | `reviewNutrition` | `/api/me/ai/nutrition-reviews` | `Authorization: Bearer {accessToken}` | POST |
| 주간 운동 평가 | `reviewExercise` | `/api/me/ai/exercise-reviews` | `Authorization: Bearer {accessToken}` | POST |

## 3. API 상세 (Notion 정리용)

### 1) 주간 통계 조회
- **기능**: 특정 날짜가 속한 주(월~일)의 일자별 식단/운동 합계 조회
- **Function**: `getWeeklyStats`
- **API Path**: `/api/me/stats/week?date=YYYY-MM-DD`

#### 요청 헤더
| 이름 | 값 | 비고 |
| --- | --- | --- |
| Authorization | `Bearer {accessToken}` | 필수 |

#### Request Body
- 없음 (`date`는 Query String)

#### Response
**🟩 200 OK**
```json
{
  "weekStartDate": "2025-12-15",
  "weekEndDate": "2025-12-21",
  "dietStats": [
    { "date": "2025-12-15", "dayOfWeek": "월요일", "carbs": 130.0, "protein": 90.0, "fat": 40.0, "calories": 1600.0 },
    { "date": "2025-12-16", "dayOfWeek": "화요일", "carbs": 0.0, "protein": 0.0, "fat": 0.0, "calories": 0.0 }
  ],
  "exerciseStats": [
    { "date": "2025-12-15", "dayOfWeek": "월요일", "durationMinutes": 45.0, "calories": 320.0 },
    { "date": "2025-12-16", "dayOfWeek": "화요일", "durationMinutes": 0.0, "calories": 0.0 }
  ]
}
```

---

### 2) AI 추천 식단 계획
- **기능**: 하루 1회, 건강정보+주간 통계 기반 식단 추천 생성 및 저장
- **Function**: `generateDailyPlan`
- **API Path**: `/api/me/ai/meal-plans`

#### 요청 헤더
| 이름 | 값 | 비고 |
| --- | --- | --- |
| Authorization | `Bearer {accessToken}` | 필수 |

#### Request Body
```json
{
  "date": "2025-12-19" // 생략 시 오늘(Asia/Seoul)
}
```

#### Response
**🟩 200 OK**
```json
{
  "planId": 1,
  "date": "2025-12-19",
  "dayOfWeek": "금요일",
  "model": "gemini-1.5-flash",
  "meals": [
    {"mealType": "breakfast", "menu": "현미밥, 닭가슴살, 샐러드", "calories": 500.0, "note": "단백질 중심"},
    {"mealType": "lunch", "menu": "연어 스테이크, 퀴노아", "calories": 650.0, "note": "오메가3 풍부"},
    {"mealType": "dinner", "menu": "채소 스튜", "calories": 450.0, "note": "저칼로리"}
  ],
  "rawMessage": "...Gemini 원문..."
}
```

---

### 3) AI 챗봇 질문/상담
- **기능**: 건강정보+주간 통계를 컨텍스트로 Gemini에게 질문, Q/A 저장
- **Function**: `ask`
- **API Path**: `/api/me/ai/chats`

#### 요청 헤더
| 이름 | 값 | 비고 |
| --- | --- | --- |
| Authorization | `Bearer {accessToken}` | 필수 |

#### Request Body
```json
{
  "message": "이번 주 식단이 단백질이 부족한가요?",
  "conversationDate": "2025-12-19" // 생략 시 오늘
}
```

#### Response (봇 메시지)
**🟩 200 OK**
```json
{
  "messageId": 23,
  "role": "BOT",
  "content": "이번 주는 단백질이 다소 부족합니다...",
  "conversationDate": "2025-12-19",
  "createdAt": "2025-12-19T08:30:12"
}
```

#### 히스토리 조회
- **Function**: `getHistory`
- **API Path**: `/api/me/ai/chats?date=YYYY-MM-DD`
- **Method**: GET
- **Response**: `ChatMessageResponse[]` (USER/BOT 순서, createdAt 오름차순)

---

### 4) 주간 영양 AI 평가
- **기능**: 주간(월~일) 식단 정보를 오늘까지 반영해 영양(탄/단/지/칼로리) 부족·적당·과다 평가
- **Function**: `reviewNutrition`
- **API Path**: `/api/me/ai/nutrition-reviews`

#### 요청 헤더/바디
| 이름 | 값 | 비고 |
| --- | --- | --- |
| Authorization | `Bearer {accessToken}` | 필수 |
| Body | `{ "date": "2025-12-19" }` | 생략 시 오늘 |

#### Response
**🟩 200 OK**
```json
{
  "reviewId": 5,
  "targetDate": "2025-12-19",
  "weekStartDate": "2025-12-15",
  "weekEndDate": "2025-12-21",
  "assessment": "{\"carbs\":\"적당\",\"protein\":\"부족\",\"fat\":\"적당\",\"calories\":\"적당\"}",
  "guidance": "주말에 단백질 위주의 한 끼를 추가하세요.",
  "model": "gemini-1.5-flash",
  "rawMessage": "{...원문...}"
}
```

---

### 5) 주간 운동 AI 평가
- **기능**: 주간 운동량(오늘까지)을 바탕으로 부족/적당/많음 평가 및 운동 추천 저장
- **Function**: `reviewExercise`
- **API Path**: `/api/me/ai/exercise-reviews`

#### 요청 헤더/바디
| 이름 | 값 | 비고 |
| --- | --- | --- |
| Authorization | `Bearer {accessToken}` | 필수 |
| Body | `{ "date": "2025-12-19" }` | 생략 시 오늘 |

#### Response
**🟩 200 OK**
```json
{
  "reviewId": 8,
  "targetDate": "2025-12-19",
  "weekStartDate": "2025-12-15",
  "weekEndDate": "2025-12-21",
  "assessment": "이번 주는 운동량이 적당합니다.",
  "recommendation": "금요일에는 20분 인터벌 러닝을, 주말에는 30분 스트레칭을 권장합니다.",
  "model": "gemini-1.5-flash",
  "rawMessage": "{...원문...}"
}
```

## 4. Postman 테스트 가이드
1. **환경 준비**: Postman 환경 변수에 `baseUrl`(예: `http://localhost:8080`), `accessToken`을 설정합니다.
2. **공통 헤더**: 모든 요청에 `Authorization: Bearer {{accessToken}}` 추가.
3. **주간 통계**
   - Method: GET, URL: `{{baseUrl}}/api/me/stats/week?date=2025-12-19`
   - 성공 시 `dietStats`, `exerciseStats` 배열이 7건씩 포함되는지 확인.
4. **AI 식단 계획**
   - Method: POST, URL: `{{baseUrl}}/api/me/ai/meal-plans`
   - Body(raw/JSON): `{ "date": "2025-12-19" }` (옵션)
   - 이미 생성된 날짜라면 같은 내용이 반환되는지 확인.
5. **AI 챗봇**
   - 질문: POST `{{baseUrl}}/api/me/ai/chats` Body: `{ "message": "단백질 보충 방법?" }`
   - 히스토리: GET `{{baseUrl}}/api/me/ai/chats?date=2025-12-19` 로 USER/BOT 메시지 순서 확인.
6. **주간 영양 평가**
   - POST `{{baseUrl}}/api/me/ai/nutrition-reviews` Body: `{ "date": "2025-12-19" }`
   - `assessment` 필드가 JSON 문자열 형태로 반환되는지 확인.
7. **주간 운동 평가**
   - POST `{{baseUrl}}/api/me/ai/exercise-reviews` Body: `{ "date": "2025-12-19" }`
   - 추천 문구(`recommendation`)가 포함되는지 확인.

## 5. Gemini API Key 설정 위치
- `GeminiClient`는 `gemini.api.key` 프로퍼티를 사용합니다.
- `.env` 또는 환경변수에 `GEMINI_API_KEY=your-key`를 넣으면 `application.yml`의 `spring.config.import` 옵션으로 자동 로드됩니다.
- 키가 없으면 호출을 건너뛰고 `[SKIPPED: Gemini API key not configured]` 문자열을 반환하여 안전하게 동작합니다.

## 6. 신규 테이블 구조
모든 테이블은 `db/20250205_ai_features.sql`에 정의되어 있으며, 실행 순서는 `init.sql -> 기존 리팩토링 SQL -> 20250205_ai_features.sql` 입니다.

### ai_daily_meal_plans
| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | BIGINT PK | 식단 계획 ID |
| email | VARCHAR(255) | 사용자 이메일 (accounts.email FK 개념) |
| plan_date | DATE | 식단이 적용되는 날짜, 사용자당 1일 1행 (UNIQUE) |
| model | VARCHAR(100) | Gemini 모델명 |
| breakfast_menu/lunch_menu/dinner_menu | TEXT | 각 식사의 추천 메뉴(쉼표 구분 문자열) |
| breakfast_calories/lunch_calories/dinner_calories | DOUBLE | 각 식사 칼로리 총합 |
| breakfast_summary/lunch_summary/dinner_summary | VARCHAR(255) | 식사별 한줄 코멘트 |
| raw_response | LONGTEXT | Gemini 원문 응답 저장 |
| request_context | JSON | 프롬프트에 사용한 날짜/메타데이터 |
| created_at/updated_at | TIMESTAMP | 생성/갱신 시각 |

### ai_chat_messages
| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | BIGINT PK | 메시지 ID |
| email | VARCHAR(255) | 사용자 이메일 |
| conversation_date | DATE | 대화 날짜(히스토리 조회 기준) |
| role | VARCHAR(20) | `USER` or `BOT` |
| message | TEXT | 질문 또는 답변 내용 |
| request_context | JSON | 질문에 함께 보낸 건강/통계 요약 |
| created_at | TIMESTAMP | 생성 시각 (정렬용 인덱스 포함) |

### ai_weekly_nutrition_reviews
| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | BIGINT PK | 평가 ID |
| email | VARCHAR(255) | 사용자 이메일 |
| target_date | DATE | 평가 기준 날짜 (주차 결정) |
| week_start_date/week_end_date | DATE | 주간 범위 (월~일) |
| model | VARCHAR(100) | Gemini 모델명 |
| nutrition_assessment | JSON | 탄/단/지/칼로리별 평가 요약 (JSON 문자열 저장) |
| guidance | TEXT | 추가 권고 문구 |
| request_context | JSON | 프롬프트 스냅샷 |
| created_at | TIMESTAMP | 생성 시각 |

### ai_weekly_exercise_reviews
| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | BIGINT PK | 평가 ID |
| email | VARCHAR(255) | 사용자 이메일 |
| target_date | DATE | 평가 기준 날짜 |
| week_start_date/week_end_date | DATE | 주간 범위 (월~일) |
| model | VARCHAR(100) | Gemini 모델명 |
| exercise_assessment | JSON | 운동량 평가 결과 |
| recommendation | TEXT | 추천 운동/루틴 |
| request_context | JSON | 프롬프트 스냅샷 |
| created_at | TIMESTAMP | 생성 시각 |

