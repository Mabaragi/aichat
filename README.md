# AI Debate Platform

사용자가 주제와 캐릭터를 정하면, 두 AI 캐릭터가 정해진 순서에 따라 토론하는 웹 서비스입니다.

일반적인 챗봇처럼 사용자가 AI와 직접 대화하는 구조보다는, AI 캐릭터끼리 만들어내는 콘텐츠를 사용자가 보는 쪽에 가깝습니다. MVP에서는 가장 단순한 형태인 **두 캐릭터 토론**에 집중합니다.

아직 개발 중인 프로젝트라서 API, UI, 도메인 구조는 계속 바뀔 수 있습니다.

## 지금 만들고 있는 것

현재 목표는 거창한 멀티 에이전트 플랫폼을 한 번에 만드는 것이 아니라, 아래 흐름이 실제로 동작하는 최소 제품을 만드는 것입니다.

1. 사용자가 계정을 만든다.
2. AI 캐릭터를 만든다.
3. 토론 주제와 형식을 정한다.
4. 토론에 참여할 캐릭터 2명을 고른다.
5. 각 캐릭터가 순서대로 발화한다.
6. 생성된 토론을 저장하고 다시 볼 수 있다.
7. 공개된 토론이나 캐릭터를 탐색할 수 있다.

핵심은 “AI가 대답을 잘하느냐”보다는, 여러 AI 참가자가 하나의 세션 안에서 규칙을 지키며 상호작용하는 구조를 잡는 것입니다.

## MVP 범위

현재 MVP에서 다루는 기능입니다.

- 계정 생성과 로그인
- AI 캐릭터 생성, 조회, 수정, 삭제
- 캐릭터 공개/비공개 설정
- 토론 세션 생성
- 두 명의 토론 참가자 설정
- 토론 시작, 턴 생성, 턴 목록 조회
- 토론 완료 처리
- 공개 토론/공개 캐릭터 탐색
- backend와 frontend가 공유하는 API contract
- GitHub Actions 기반 테스트/빌드 검증

의도적으로 아직 넣지 않은 것들도 있습니다.

- 실시간 스트리밍
- 소셜 로그인
- 캐릭터 장기 기억
- 캐릭터 관계도
- 세계관 설정
- 복잡한 추천 알고리즘
- 커뮤니티 피드
- 결제
- 관리자 페이지

이런 기능들은 나중에 필요가 분명해지면 붙일 예정입니다.

## Repository 구조

```text
.
├── backend/      # Spring Boot API, CLI, domain/application 코드
├── frontend/     # Next.js UI, BFF Route Handler
├── docs/         # 공통 문서, API contract, 배포 문서
├── infra/        # AWS 배포용 Terraform
└── .github/      # CI / release workflow
```

루트 문서는 전체 방향만 다룹니다. 세부 개발 문서는 각 프로젝트 안에 따로 둡니다.

- backend 개발: `backend/README.md`
- frontend 개발: `frontend/README.md`
- API contract: `docs/api/README.md`
- 배포 메모: `docs/deployment/aws-cicd.md`
- MVP 제품 명세: `ai_debate_platform_mvp_spec.md`

## 기술 스택

### Backend

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- SQLite
- Flyway
- SpringDoc OpenAPI
- Maven

Backend는 도메인별로 대략 아래 계층을 나눕니다.

```text
domain          # entity, enum, repository contract, 도메인 규칙
application     # use case, transaction boundary
infrastructure  # persistence, external provider adapter
web             # controller, request/response DTO
```

주요 package는 다음과 같습니다.

- `auth`: 계정과 인증 흐름
- `user`: 사용자 정보
- `character`: AI 캐릭터 관리
- `debate`: 토론 세션, 참가자, 턴 생성
- `generation`: 텍스트 생성 provider 추상화
- `category`: 공개 탐색용 카테고리
- `share`: 공유 링크 관련 기능
- `common`: 공통 예외, actor, 시간 추상화

### Frontend

- Next.js 16 App Router
- React 19
- TypeScript
- Tailwind CSS 4
- SWR
- Vitest
- OpenAPI TypeScript

Frontend는 Next.js Route Handler를 중간 계층으로 두고 Spring API와 통신합니다. 개발 중에는 mock mode로 frontend만 띄울 수 있고, 필요하면 Spring backend와 연결해서 실행할 수 있습니다.

### Infra

- AWS EC2
- AWS ECR
- GitHub Actions
- Terraform

현재 배포 구조는 단순하게 유지합니다.

```text
Internet :80
    |
    v
frontend container :3000
    |
    v
backend container :8080
    |
    v
SQLite file on mounted data volume
```

## 로컬 실행

### Backend

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

backend는 기본적으로 `8080` 포트에서 실행됩니다. 개발용 profile을 사용하면 외부 생성 모델 없이 기본 흐름을 확인할 수 있습니다.

### Frontend

```powershell
cd frontend
Copy-Item .env.example .env.local
npm ci
npm run generate:api
npm run dev
```

기본 `npm run dev`는 mock mode로 실행됩니다. Spring backend와 연결해서 확인하려면 backend를 `localhost:8080`에서 띄운 뒤 아래 명령을 사용합니다.

```powershell
npm run dev:backend
```

frontend는 기본적으로 `3000` 포트에서 실행됩니다.

## 자주 쓰는 명령

### Backend

```powershell
cd backend
.\mvnw.cmd test
```

Docker image를 직접 빌드할 때는 저장소 루트에서 실행합니다.

```powershell
docker build -t aichat:local backend
```

### Frontend

```powershell
cd frontend
npm run lint
npm run typecheck
npm run test
npm run build
```

한 번에 확인하려면 다음 명령을 사용합니다.

```powershell
npm run check
```

## API contract

Backend와 frontend는 `docs/api/openapi.json`을 기준으로 API 형태를 맞춥니다.

공개 API에 영향을 주는 변경을 했다면 backend에서 contract 파일을 다시 생성합니다.

```powershell
cd backend
.\mvnw.cmd -Dtest=OpenApiContractExportTest "-Dopenapi.output=../docs/api/openapi.json" test
cd ..
```

그 다음 frontend 타입을 다시 생성합니다.

```powershell
cd frontend
npm run generate:api
```

생성된 타입 파일도 같이 커밋합니다.

## 설계하면서 신경 쓴 부분

### 토론 세션은 생성 시점의 캐릭터 스냅샷을 가진다

토론 참가자는 원본 캐릭터를 직접 참조하지 않고, 세션 생성 시점의 이름, 설명, persona 등을 복사해서 보관합니다.

이렇게 한 이유는 단순합니다.

- 나중에 캐릭터가 수정돼도 이미 생성된 토론 내용은 바뀌면 안 된다.
- 캐릭터가 삭제돼도 과거 토론은 읽을 수 있어야 한다.
- 같은 캐릭터를 서로 다른 모델 설정으로 넣을 수 있어야 한다.
- 토론 발화 순서를 세션 안에서 안정적으로 고정해야 한다.

### generation은 도메인 바깥 capability로 둔다

`generation`은 토론 도메인 자체가 아니라 텍스트 생성 provider를 감싸는 지원 모듈입니다.

토론 프롬프트 정책은 `debate` 쪽에서 관리하고, `generation`은 provider 차이를 숨기는 역할만 맡습니다.

### frontend는 backend API를 직접 호출하지 않는다

브라우저에서 Spring API를 직접 호출하지 않고 Next.js Route Handler를 한 번 거칩니다.

이 구조를 택한 이유는 frontend 쪽 인증 흐름이나 공개 API 프록시 정책을 나중에 바꾸기 쉽게 하기 위해서입니다.

## 앞으로 해볼 것

MVP가 안정화되면 아래 기능을 검토할 예정입니다.

- 토론 세션 공개 페이지
- 사용자가 공개 토론을 탐색하는 화면
- 사용자가 토론 중간에 개입하는 기능
- AI judge가 토론을 조율하는 구조
- 마음에 드는 토론 구간 저장
- 여러 명이 참여하는 토론
- 상황극/소설 생성
- 마피아 게임 같은 규칙 기반 시뮬레이션

지금은 이 확장 기능들을 바로 구현하기보다, 두 캐릭터 토론 루프를 먼저 단단하게 만드는 쪽을 우선합니다.
