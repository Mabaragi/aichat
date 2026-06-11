import type { components } from "@/api/generated/backend-schema";

export type AuthTokensResponse = components["schemas"]["AuthTokensResponse"];
export type CategorySummaryResponse =
  components["schemas"]["CategorySummaryResponse"];
export type CharacterResponse = components["schemas"]["CharacterResponse"];
export type CompleteDebateSessionResponse =
  components["schemas"]["CompleteDebateSessionResponse"];
export type CreateCharacterRequest =
  components["schemas"]["CreateCharacterRequest"];
export type CreateDebateSessionRequest =
  components["schemas"]["CreateDebateSessionRequest"];
export type DebateSessionResponse =
  components["schemas"]["DebateSessionResponse"];
export type DebateTurnResponse = components["schemas"]["DebateTurnResponse"];
export type GenerateTurnResponse = components["schemas"]["GenerateTurnResponse"];
export type LoginRequest = components["schemas"]["LoginRequest"];
export type PublicCharacterPageResponse =
  components["schemas"]["PublicCharacterPageResponse"];
export type PublicDebateSessionPageResponse =
  components["schemas"]["PublicDebateSessionPageResponse"];
export type RefreshTokenRequest =
  components["schemas"]["RefreshTokenRequest"];
export type SignupRequest = components["schemas"]["SignupRequest"];
export type StartDebateSessionResponse =
  components["schemas"]["StartDebateSessionResponse"];
export type UserResponse = components["schemas"]["UserResponse"];
type PersonaPayload = components["schemas"]["PersonaPayload"];

export type MockUserRecord = {
  user: UserResponse;
  password: string;
};

export type MockAuthSession = {
  userId: number;
  accessToken: string;
  refreshToken: string;
};

export type MockDebateTurn = GenerateTurnResponse &
  Pick<DebateTurnResponse, "participantModel">;

export type MockState = {
  categories: CategorySummaryResponse[];
  users: MockUserRecord[];
  characters: CharacterResponse[];
  debateSessions: DebateSessionResponse[];
  debateTurns: MockDebateTurn[];
  authSessions: MockAuthSession[];
  nextCharacterId: number;
  nextDebateSessionId: number;
  nextParticipantId: number;
  nextTurnId: number;
  nextUserId: number;
  nextTokenId: number;
};

const BASE_TIME = "2026-06-11T09:00:00";

const categorySeeds = [
  { id: 1, scope: "DEBATE", slug: "food", name: "음식" },
  { id: 2, scope: "DEBATE", slug: "culture", name: "문화" },
  { id: 3, scope: "DEBATE", slug: "tech", name: "기술" },
  { id: 4, scope: "DEBATE", slug: "life", name: "생활" },
  { id: 5, scope: "DEBATE", slug: "society", name: "사회" },
  { id: 6, scope: "DEBATE", slug: "fun", name: "재미" },
  { id: 7, scope: "DEBATE", slug: "other", name: "기타" },
  { id: 8, scope: "CHARACTER", slug: "expert", name: "전문가" },
  { id: 9, scope: "CHARACTER", slug: "critic", name: "비평가" },
  { id: 10, scope: "CHARACTER", slug: "creator", name: "창작자" },
  { id: 11, scope: "CHARACTER", slug: "storyteller", name: "스토리텔러" },
  { id: 12, scope: "CHARACTER", slug: "comedy", name: "코미디" },
  { id: 13, scope: "CHARACTER", slug: "utility", name: "실용" },
  { id: 14, scope: "CHARACTER", slug: "other", name: "기타" },
] satisfies CategorySummaryResponse[];

const demoUser = {
  id: 1,
  email: "demo@example.com",
  nickname: "데모 토론가",
  createdAt: BASE_TIME,
} satisfies UserResponse;

function persona(
  identity: string,
  debateRole: string,
  tone: string,
  coreValues: string[] = ["사실성"],
): PersonaPayload {
  return {
    identity,
    debateRole,
    coreValues,
    expertise: [],
    defaultStance: "논제에 따라 입장을 형성한다.",
    evidenceStyle: "근거와 논리를 우선한다.",
    debateBehavior: ["상대 주장의 핵심 전제를 확인한다."],
    voiceStyle: {
      tone,
      sentenceLength: "중간",
      rhetoricalStyle: "질문과 구조적 반박 중심",
      signaturePhrases: [],
    },
    boundaries: {
      mustDo: ["상대 주장을 먼저 요약한다.", "불확실한 사실은 단정하지 않는다."],
      mustNotDo: ["인신공격하지 않는다.", "출처 없는 수치를 만들지 않는다."],
    },
    exampleLines: [],
  };
}

const characterSeeds = [
  {
    id: 1,
    ownerId: 1,
    name: "합리적 미식가",
    description: "음식 취향을 근거와 경험으로 차분하게 분석합니다.",
    category: categorySeeds[7],
    persona: persona("합리적 미식가", "데이터 분석가", "차분함", ["실증성", "경험 품질"]),
    visibility: "PRIVATE",
    createdAt: BASE_TIME,
    updatedAt: BASE_TIME,
  },
  {
    id: 2,
    ownerId: 1,
    name: "직관형 리뷰어",
    description: "첫인상과 감각적 표현으로 빠르게 반박합니다.",
    category: categorySeeds[8],
    persona: persona("직관형 리뷰어", "시민 대표", "직설적", ["생활감", "명료함"]),
    visibility: "PRIVATE",
    createdAt: BASE_TIME,
    updatedAt: BASE_TIME,
  },
  {
    id: 101,
    ownerId: 99,
    name: "테크 낙관론자",
    description: "새 기술의 가능성을 실제 사용 장면 중심으로 설명합니다.",
    category: categorySeeds[7],
    persona: persona("테크 낙관론자", "미래학자", "전문적", ["가능성", "장기 영향"]),
    visibility: "PUBLIC",
    createdAt: BASE_TIME,
    updatedAt: BASE_TIME,
  },
  {
    id: 102,
    ownerId: 99,
    name: "느긋한 현실주의자",
    description: "과장된 주장에 속도를 늦추고 비용과 지속성을 따집니다.",
    category: categorySeeds[8],
    persona: persona("느긋한 현실주의자", "회의적 반대자", "차분함", ["검증", "지속성"]),
    visibility: "PUBLIC",
    createdAt: BASE_TIME,
    updatedAt: BASE_TIME,
  },
  {
    id: 103,
    ownerId: 99,
    name: "이야기 장인",
    description: "딱딱한 쟁점도 장면과 비유로 풀어내는 토론자입니다.",
    category: categorySeeds[10],
    persona: persona("이야기 장인", "스토리텔러", "친근함", ["공감", "전달력"]),
    visibility: "PUBLIC",
    createdAt: BASE_TIME,
    updatedAt: BASE_TIME,
  },
  {
    id: 104,
    ownerId: 99,
    name: "한 줄 개그맨",
    description: "핵심을 짧게 찌르되 분위기를 가볍게 만듭니다.",
    category: categorySeeds[11],
    persona: persona("한 줄 개그맨", "도발적 비판자", "직설적", ["긴장감", "유머"]),
    visibility: "PUBLIC",
    createdAt: BASE_TIME,
    updatedAt: BASE_TIME,
  },
] satisfies CharacterResponse[];

const debateSeeds = [
  {
    id: 201,
    ownerId: 1,
    topicTitle: "부먹 vs 찍먹",
    topicDescription: "탕수육 소스를 부어 먹는 방식과 찍어 먹는 방식 중 무엇이 더 나은가?",
    topicCategory: "food",
    category: categorySeeds[0],
    status: "COMPLETED",
    visibility: "PUBLIC",
    format: "PROS_AND_CONS",
    maxRounds: 2,
    currentRound: 2,
    maxTurnLength: 600,
    participants: [
      participant(301, 101, 0, "FAST", "테크 낙관론자"),
      participant(302, 102, 1, "QUALITY", "느긋한 현실주의자"),
    ],
    createdAt: "2026-06-11T09:10:00",
    startedAt: "2026-06-11T09:11:00",
    endedAt: "2026-06-11T09:16:00",
  },
  {
    id: 202,
    ownerId: 1,
    topicTitle: "AI 글쓰기는 창작을 돕는가",
    topicDescription: "AI 보조 도구가 작가의 고유한 목소리를 강화하는지 약화하는지 토론합니다.",
    topicCategory: "culture",
    category: categorySeeds[1],
    status: "COMPLETED",
    visibility: "PUBLIC",
    format: "FREE_DISCUSSION",
    maxRounds: 2,
    currentRound: 2,
    maxTurnLength: 700,
    participants: [
      participant(303, 103, 0, "BALANCED", "이야기 장인"),
      participant(304, 102, 1, "QUALITY", "느긋한 현실주의자"),
    ],
    createdAt: "2026-06-11T10:00:00",
    startedAt: "2026-06-11T10:01:00",
    endedAt: "2026-06-11T10:07:00",
  },
  {
    id: 203,
    ownerId: 1,
    topicTitle: "재택근무는 생산성을 높이는가",
    topicDescription: "집중 시간, 협업 비용, 팀 문화 관점에서 재택근무의 효과를 따집니다.",
    topicCategory: "life",
    category: categorySeeds[3],
    status: "COMPLETED",
    visibility: "PUBLIC",
    format: "PROS_AND_CONS",
    maxRounds: 2,
    currentRound: 2,
    maxTurnLength: 650,
    participants: [
      participant(305, 101, 0, "FAST", "테크 낙관론자"),
      participant(306, 104, 1, "MOCK", "한 줄 개그맨"),
    ],
    createdAt: "2026-06-11T11:00:00",
    startedAt: "2026-06-11T11:01:00",
    endedAt: "2026-06-11T11:05:00",
  },
] satisfies DebateSessionResponse[];

const turnSeeds = [
  turn(401, 201, 301, "FAST", 1, 1, "ARGUMENT", "부먹은 소스와 튀김을 하나의 요리로 완성합니다."),
  turn(402, 201, 302, "QUALITY", 1, 2, "REBUTTAL", "찍먹은 바삭함과 농도 조절권을 모두 지킵니다."),
  turn(403, 201, 301, "FAST", 2, 3, "ARGUMENT", "소스가 스며든 식감도 탕수육 경험의 중요한 축입니다."),
  turn(404, 201, 302, "QUALITY", 2, 4, "SUMMARY", "완성도보다 선택권을 중시한다면 찍먹이 더 설득력 있습니다."),
  turn(405, 202, 303, "BALANCED", 1, 1, "ARGUMENT", "AI는 초안을 빠르게 만들지만 최종 목소리는 사람이 결정합니다."),
  turn(406, 202, 304, "QUALITY", 1, 2, "REBUTTAL", "도움이 반복되면 문장 선택의 근육이 약해질 수 있습니다."),
  turn(407, 202, 303, "BALANCED", 2, 3, "ARGUMENT", "반대로 반복 수정 과정은 작가가 취향을 더 선명히 보게 합니다."),
  turn(408, 202, 304, "QUALITY", 2, 4, "SUMMARY", "도구의 속도보다 사용자의 편집 기준이 핵심 변수입니다."),
  turn(409, 203, 305, "FAST", 1, 1, "ARGUMENT", "재택근무는 출퇴근 비용을 줄이고 깊은 집중 시간을 늘립니다."),
  turn(410, 203, 306, "MOCK", 1, 2, "REBUTTAL", "회의가 집까지 따라오면 사무실보다 퇴근이 더 어렵습니다."),
  turn(411, 203, 305, "FAST", 2, 3, "ARGUMENT", "비동기 문화를 갖추면 협업 기록도 더 명확해집니다."),
  turn(412, 203, 306, "MOCK", 2, 4, "SUMMARY", "재택의 생산성은 제도가 아니라 팀의 습관에서 갈립니다."),
] satisfies MockDebateTurn[];

export function createSeedMockState(): MockState {
  return cloneJson({
    categories: categorySeeds,
    users: [{ user: demoUser, password: "password123" }],
    characters: characterSeeds,
    debateSessions: debateSeeds,
    debateTurns: turnSeeds,
    authSessions: [],
    nextCharacterId: 1001,
    nextDebateSessionId: 2001,
    nextParticipantId: 3001,
    nextTurnId: 4001,
    nextUserId: 2,
    nextTokenId: 1,
  });
}

function participant(
  id: number,
  sourceCharacterId: number,
  position: number,
  model: NonNullable<
    NonNullable<DebateSessionResponse["participants"]>[number]["model"]
  >,
  name: string,
): NonNullable<DebateSessionResponse["participants"]>[number] {
  const source = characterSeeds.find((character) => character.id === sourceCharacterId);
  return {
    id,
    sourceCharacterId,
    position,
    model,
    name,
    description: source?.description,
    persona: source?.persona,
  };
}

function turn(
  id: number,
  sessionId: number,
  participantId: number,
  participantModel: NonNullable<DebateTurnResponse["participantModel"]>,
  round: number,
  turnIndex: number,
  type: NonNullable<DebateTurnResponse["type"]>,
  content: string,
): MockDebateTurn {
  return {
    id,
    sessionId,
    participantId,
    participantModel,
    round,
    turnIndex,
    type,
    status: "COMPLETED",
    content,
    modelName: `mock-${participantModel.toLowerCase()}`,
    inputTokens: 0,
    outputTokens: 0,
    createdAt: "2026-06-11T09:12:00",
  };
}

function cloneJson<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}
