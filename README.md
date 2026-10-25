# 생각서랍 (Thought Index)

생각을 파스텔 인덱스 탭에 꽂아 정리하는 메모 앱.

- **backend/** Spring Boot 4.1 · Java 21 · JPA · Flyway · Spring Security(JWT, OAuth2) · H2(로컬) / PostgreSQL(운영)
- **frontend/** Vue 3 · Vite · Pinia · Vue Router · PWA

## 실행

```bash
# 1) 백엔드 (http://localhost:8080)
cd backend && ./gradlew bootRun

# 2) 프론트 (http://localhost:5190) — Node 22 필요
cd frontend && nvm use && npm install && npm run dev
```

로컬 데모 계정: `demo@thoughtindex.local` / `demo1234` (local 프로필에서만 생성)

테스트: `cd backend && ./gradlew test`
로컬 DB 보기: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/thought-index`, user `sa`)

## 백엔드 구조 — 이렇게 읽어 보세요

```
com.thoughtindex
├── global/                  여러 도메인이 같이 쓰는 것
│   ├── config/              JPA Auditing, CORS(@ConfigurationProperties)
│   ├── common/              BaseTimeEntity(생성·수정 시각), PageResponse
│   └── error/               ErrorCode · BusinessException · GlobalExceptionHandler
├── tab/                     인덱스 탭 도메인
│   ├── api/                 Controller + dto (요청/응답 record)
│   ├── application/         Service (비즈니스 규칙, 트랜잭션)
│   └── domain/              Entity · Repository · 도메인 이벤트
└── note/                    메모 도메인 (구조 동일)
```

**계층별이 아니라 도메인별로 패키지를 나눈 이유**
`controller/ service/ repository/` 로 나누면 기능 하나 고칠 때 폴더 세 개를 오가야 하고, 앱이 커지면 각 폴더가 수십 개 파일로 불어난다.
도메인별로 묶으면 "탭 기능"이 `tab/` 안에서 끝나고, 나중에 모듈로 떼어내기도 쉽다.

### 요청 하나가 지나가는 길

```
POST /api/notes
 → NoteController      @Valid 로 입력 검증 (실패 시 400 + fieldErrors)
 → NoteService         @Transactional — 탭이 있는지 확인, 엔티티 생성
 → NoteRepository      저장
 ← NoteResponse        엔티티 대신 DTO 로 응답 (201 Created + Location)
```

예외는 서비스가 `BusinessException(ErrorCode.XXX)` 를 던지기만 하면 `GlobalExceptionHandler` 가 일관된 모양으로 바꿔 준다.

```json
{ "code": "NOTE_NOT_FOUND", "message": "메모를 찾을 수 없어요.", "fieldErrors": [] }
```

### 꼭 짚어볼 설계 포인트

| 포인트 | 어디서 보나 | 왜 |
| --- | --- | --- |
| Entity 에 setter 없음 | `Note.edit()`, `IndexTab.update()` | 값이 "어떤 의미로" 바뀌는지 메서드 이름이 말해 준다 |
| `@Transactional(readOnly = true)` 기본 | `*Service` 클래스 상단 | 조회에서 실수로 쓰기 방지, 쓰기 메서드만 따로 켬 |
| 변경 감지 (dirty checking) | `IndexTabService.update()` | `save()` 없이도 트랜잭션 끝날 때 UPDATE |
| `@ManyToOne(fetch = LAZY)` | `Note.tab` | 기본값 EAGER 는 쓸데없는 조인/쿼리의 주범 |
| 단방향 연관관계 | `Note → IndexTab` 만 | 양방향은 필요해질 때 추가해도 늦지 않다 |
| N+1 방지 | `countNotesPerTab()`, 검색의 `join fetch` | 탭/메모 수만큼 쿼리가 늘지 않게 |
| 벌크 연산 주의 | `NoteRepository.deleteAllByTabId()` | 1차 캐시를 건너뛰므로 flush/clear 필요 (테스트로 직접 겪은 버그!) |
| 도메인 이벤트 | `TabDeletedEvent` → `TabDeletedEventListener` | tab 이 note 를 몰라도 되게 — 순환 의존 제거 |
| `@Enumerated(STRING)` | `IndexTab.color` | ORDINAL 은 enum 순서만 바꿔도 데이터가 깨짐 |
| `open-in-view: false` | `application.yml` | DB 커넥션을 응답 끝까지 붙잡지 않음 |
| `ddl-auto: validate` + Flyway | `db/migration/` | 스키마 변경 이력을 SQL 로 남기고, 운영 DB 를 Hibernate 가 건드리지 않음 |
| 프로필 분리 | `application-{local,prod}.yml` | 시드 데이터는 local 에서만, 운영 비밀번호는 환경변수로 |
| 페이지 크기 상한 | `max-page-size: 50` | `?size=100000` 같은 요청 방어 |

### 테스트 3종

- `IndexTabServiceTest` — **단위 테스트**. Mockito 로 리포지토리를 가짜로 바꾸고 규칙(탭 8개 제한, 순서 변경)만 검증. 가장 빠름.
- `NoteRepositoryTest` — **슬라이스 테스트** (`@DataJpaTest`). JPA 만 띄워서 쿼리(정렬, 검색)를 검증.
- `NoteApiIntegrationTest` — **통합 테스트** (`@SpringBootTest` + MockMvc). HTTP 요청부터 DB 까지 전체 흐름.

## API

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/tabs` | 탭 목록 (+메모 개수) |
| POST | `/api/tabs` | 탭 만들기 `{name, color}` |
| PUT | `/api/tabs/{id}` | 탭 고치기 |
| PUT | `/api/tabs/order` | 순서 바꾸기 `{tabIds: [3,1,2]}` |
| DELETE | `/api/tabs/{id}` | 탭 + 안의 메모 삭제 |
| GET | `/api/notes?tabId=1&page=0` | 탭의 메모 (고정 먼저, 최근 수정순) |
| GET | `/api/notes/search?q=...` | 제목·내용 검색 |
| GET/PUT/DELETE | `/api/notes/{id}` | 메모 조회·수정·삭제 |
| POST | `/api/notes` | 메모 쓰기 `{tabId, title, content, pinned}` |

색: `PEACH` `MINT` `SKY` `LAVENDER` `PINK` `LEMON` (hex 는 `frontend/src/constants/tabColors.js`)

## 프론트 구조

```
src/
├── api/          fetch 래퍼(client.js) + 도메인별 API
├── stores/       Pinia — 여러 화면이 공유하는 탭 목록
├── views/        NotebookView(탭+카드) · NoteEditorView(줄노트, 자동 저장) · SearchView
├── components/   IndexTabRail · NoteCard · TabFormSheet
└── constants/    파스텔 팔레트
```

## 로그인

- 이메일 + 비밀번호 → 서버가 JWT(HS256) 발급 → 화면은 `Authorization: Bearer` 로 전송 (세션 없음)
- 구글 로그인은 `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID/SECRET` 환경변수가 있을 때만 켜짐
- 탭은 `member_id` 로 주인을 갖고, 메모는 탭을 통해 주인이 정해진다. 남의 데이터는 404

## 배포 (전부 무료)

| 무엇 | 어디 | 설정 파일 |
| --- | --- | --- |
| 서버 | Render (Docker) | `render.yaml`, `backend/Dockerfile` |
| DB | Supabase PostgreSQL (Session pooler) | 환경변수 `DB_URL` `DB_USERNAME` `DB_PASSWORD` |
| 화면 | Vercel (Root Directory: `frontend`) | `frontend/vercel.json`, 환경변수 `VITE_API_BASE_URL` |

단계별 가이드는 설계 문서의 "8. 출시 가이드" 탭.

## 다음 단계 아이디어

- [ ] 메모 보관하기(archived) — 스터디 커리큘럼 7~12주 실습
- [ ] 토큰 갱신(refresh token), 비밀번호 재설정 메일
