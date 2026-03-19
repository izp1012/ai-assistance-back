# AI Assistance Backend

Spring Boot 기반의 AI 채팅 지원 백엔드 서버입니다. OpenAI GPT 모델을 활용한 실시간 AI 채팅, 사용자 맞춤 AI 구독, 키워드 분석, 동적 스케줄링 기능을 제공합니다.

## 기술 스택

- **Java 17** / **Spring Boot 3.4.2**
- **Spring AI 1.0.0-M6** — OpenAI GPT-4o 연동
- **Spring Security** — Stateless JWT 인증 + Google OAuth2
- **Spring WebSocket** — STOMP 기반 실시간 채팅
- **Spring Data JPA** — PostgreSQL / H2
- **Quartz Scheduler** — JDBC 기반 동적 스케줄링
- **SpringDoc OpenAPI 2.7.0** — Swagger UI

## 빌드 & 실행

```bash
# 빌드
./gradlew build
./gradlew clean build

# 실행
./gradlew bootRun

# 테스트
./gradlew test
./gradlew test --tests "com.uf.assistance.service.ChatKeywordServiceTest"
```

- 기본 활성 프로파일: `dev` (서버 포트: **8081**)
- Swagger UI: `http://localhost:8081/swagger-ui.html`

## 프로파일 설정

| 프로파일 | 데이터베이스 | DDL 전략 | 비고 |
|---------|------------|---------|------|
| `dev` (기본값) | PostgreSQL (원격) | update | Quartz JDBC 스토어, Debug 로깅 |
| `local` | H2 인메모리 | create | 서버 시작 시 스키마 재생성, WebSocket/Quartz 비활성 |
| `test` | PostgreSQL (원격) | update | 통합 테스트용 |

## 아키텍처

표준 Spring 레이어드 아키텍처: `Controller → Service → Repository → Entity`

```
src/main/java/com/uf/assistance/
├── web/           # REST 컨트롤러, WebSocket 핸들러
├── service/       # 비즈니스 로직 (트랜잭션)
├── domain/        # JPA 엔티티, Spring Data 레포지토리
├── config/        # Security, WebSocket, Swagger, JWT 설정
│   └── jwt/       # JWT 필터 및 토큰 프로바이더
├── batchjob/      # Quartz 스케줄 잡
├── handler/       # JWT 엔트리포인트, OAuth2 성공 핸들러
├── event/         # Spring 애플리케이션 이벤트
└── dto/           # 요청/응답 DTO
```

## 주요 기능

### 인증 & 보안
- **Stateless JWT**: `JwtAuthenticationFilter` (로그인) → `JwtAuthorizationFilter` (검증) → `JwtRequestFilter`
- **Google OAuth2** 소셜 로그인 (`OAuth2SuccessHandler`)
- **Refresh Token**: `RefreshToken` 엔티티에 저장, 재인증 시 upsert
- 공개 엔드포인트: `/api/login/**`, `/api/join/**`, `/api/oauth2/**`, `/api/image/**`, Swagger
- 인증 필요 엔드포인트: `/api/auth/**`
- WebSocket: `/chat/**`

### AI 채팅 (WebSocket / STOMP)
- STOMP 메시지 브로커: `/chat`
- 구독 경로: `/topic/chat/{subscriptionId}`
- AI 채팅 메시지 전송: `/app/chat.sendMessageAI/{subscriptionId}`
- 일반 채팅 메시지 전송: `/app/chat.sendMessage/{subscriptionId}`

### 동적 스케줄링 (Quartz)
- `DynamicSchedulerService` — 런타임에 Quartz 잡 생성/수정/삭제
- `SchedulerInitializer` — 서버 시작 시 기존 스케줄 잡 복원
- JDBC 잡 스토어(PostgreSQL)로 서버 재시작 후에도 스케줄 유지

### 키워드 & 관심사 분석
- `ChatKeywordService` — 채팅 기반 키워드 추출 및 강화
- `VectorInterestService` — Flask API 연동 벡터 임베딩 및 관심사 관리
- Flask API: `http://3.39.234.47:5001`

## 핵심 도메인 엔티티

| 엔티티 | 테이블 | 설명 |
|--------|--------|------|
| `User` | `user_tb` | 사용자 계정 및 인증 정보 |
| `Chat` | `chat_tb` | 채팅 메시지 |
| `AISubscription` | `ai_subscription_tb` | 사용자별 AI 구독 |
| `CustomAI` | `custom_ai_tb` | 사용자/구독별 맞춤 AI 설정 |
| `ScheduledJob` | `scheduled_jobs` | Quartz 잡 정의 (크론 표현식) |
| `ChatKeyword` | `chat_keyword` | 채팅-관심사 연결 |
| `RefreshToken` | — | JWT 리프레시 토큰 저장 |
| `PromptTemplate` | — | AI 프롬프트 템플릿 |
| `EnvEntry` | — | 동적 환경 변수 설정 |

## 외부 연동

| 서비스 | 주소 | 용도 |
|--------|------|------|
| OpenAI API | Spring AI 설정 참조 | GPT-4o 채팅 완성 |
| Flask API | `http://3.39.234.47:5001` | 벡터 임베딩, 관심사 분석 |
| PostgreSQL | `3.39.234.47:5432/youf` | 메인 데이터베이스 (dev/test) |
| Google OAuth2 | Google Cloud Console 설정 참조 | 소셜 로그인 |

## API 엔드포인트 요약

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/join` | 회원가입 |
| POST | `/api/login` | 로그인 (JWT 발급) |
| GET | `/api/oauth2/login/google` | Google OAuth2 로그인 |
| GET | `/api/auth/user/**` | 사용자 정보 조회/수정 |
| GET/POST | `/api/auth/ai-subscription/**` | AI 구독 관리 |
| GET/POST | `/api/auth/schedule/**` | 스케줄 잡 관리 |
| POST | `/api/image/**` | 이미지 업로드 |
| GET/PUT | `/api/auth/env/**` | 환경 변수 관리 |

## 테스트

```bash
# 전체 테스트
./gradlew test

# 개별 테스트 클래스
./gradlew test --tests "com.uf.assistance.service.ChatKeywordServiceTest"
./gradlew test --tests "com.uf.assistance.service.UserServiceTest"
./gradlew test --tests "com.uf.assistance.service.VectorInterestServiceTest"
./gradlew test --tests "com.uf.assistance.service.SpringAIOpenAIServiceTest"
```

> test 프로파일은 원격 PostgreSQL을 사용하므로 네트워크 연결이 필요합니다.
