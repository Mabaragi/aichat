import type {
  PlatformCategory,
  PlatformCharacterSpotlight,
  PlatformDebateCard,
} from "@/lib/api-types";

export const platformCategories: PlatformCategory[] = [
  {
    id: "all",
    label: "전체",
    description: "지금 바로 둘러볼 수 있는 토론 아레나",
  },
  {
    id: "food",
    label: "음식",
    description: "취향과 논리가 가장 빨리 충돌하는 주제",
  },
  {
    id: "culture",
    label: "문화",
    description: "영화, 음악, 밈, 취미를 두고 벌이는 해석 싸움",
  },
  {
    id: "tech",
    label: "기술",
    description: "AI, 플랫폼, 제품 선택을 검증하는 논쟁",
  },
  {
    id: "life",
    label: "생활",
    description: "일상 선택을 그럴듯하게 밀어붙이는 토론",
  },
  {
    id: "weird",
    label: "병맛",
    description: "진지해서 더 이상한 가벼운 논쟁",
  },
];

// TODO: Replace with GET /api/public/debate-sessions?sort=featured once the backend exposes public discovery.
export const platformDebates: PlatformDebateCard[] = [
  {
    id: "sauce-first",
    title: "탕수육은 부먹인가 찍먹인가",
    description:
      "소스가 튀김의 일부인지, 바삭함을 보존해야 하는지 AI 캐릭터가 끝까지 밀어붙입니다.",
    categoryId: "food",
    categoryLabel: "음식",
    status: "HOT",
    format: "PROS_AND_CONS",
    participants: ["합리적 미식가", "바삭함 수호자"],
    models: ["FAST", "QUALITY"],
    stats: [
      { label: "참여", value: "18.4K" },
      { label: "턴", value: "92" },
      { label: "승률", value: "52:48" },
    ],
    accent: "#ff6b35",
  },
  {
    id: "ai-writer",
    title: "AI 글쓰기는 창작을 죽이는가",
    description:
      "생산성 도구인지, 표현을 납작하게 만드는 지름길인지 서로 다른 관점으로 검증합니다.",
    categoryId: "tech",
    categoryLabel: "기술",
    status: "LIVE",
    format: "FREE_DISCUSSION",
    participants: ["프로덕트 낙관론자", "편집자 캐릭터"],
    models: ["BALANCED", "QUALITY"],
    stats: [
      { label: "조회", value: "7.8K" },
      { label: "라운드", value: "6" },
      { label: "댓글", value: "214" },
    ],
    accent: "#38bdf8",
  },
  {
    id: "morning-night",
    title: "아침형 인간은 정말 더 생산적인가",
    description:
      "수면 리듬, 집중력, 사회적 일정이라는 세 축으로 생활 루틴을 해부합니다.",
    categoryId: "life",
    categoryLabel: "생활",
    status: "READY",
    format: "PROS_AND_CONS",
    participants: ["새벽 루틴러", "야행성 전략가"],
    models: ["FAST", "BALANCED"],
    stats: [
      { label: "참여", value: "3.2K" },
      { label: "턴", value: "44" },
      { label: "북마크", value: "610" },
    ],
    accent: "#facc15",
  },
  {
    id: "movie-spoiler",
    title: "스포일러는 작품 감상을 망치는가",
    description:
      "결말을 아는 상태에서도 서사가 작동하는지, 첫 경험이 절대적인지 겨룹니다.",
    categoryId: "culture",
    categoryLabel: "문화",
    status: "NEW",
    format: "FREE_DISCUSSION",
    participants: ["서사 분석가", "첫 관람 순정파"],
    models: ["QUALITY", "FAST"],
    stats: [
      { label: "조회", value: "12.1K" },
      { label: "턴", value: "31" },
      { label: "공유", value: "1.1K" },
    ],
    accent: "#a78bfa",
  },
  {
    id: "pineapple-pizza",
    title: "파인애플 피자는 피자인가",
    description:
      "단짠 조합의 정당성과 피자 정체성의 경계를 웃기지만 꽤 논리적으로 다룹니다.",
    categoryId: "weird",
    categoryLabel: "병맛",
    status: "HOT",
    format: "PROS_AND_CONS",
    participants: ["하와이안 변호인", "정통파 도우 장인"],
    models: ["MOCK", "FAST"],
    stats: [
      { label: "참여", value: "21.7K" },
      { label: "턴", value: "120" },
      { label: "분노", value: "높음" },
    ],
    accent: "#fb7185",
  },
  {
    id: "phone-case",
    title: "휴대폰 케이스는 필수인가",
    description:
      "디자인, 수리비, 그립감, 소유감까지 사소하지만 은근히 갈리는 선택을 토론합니다.",
    categoryId: "life",
    categoryLabel: "생활",
    status: "READY",
    format: "FREE_DISCUSSION",
    participants: ["생폰 미니멀리스트", "안전제일 실용파"],
    models: ["FAST", "FAST"],
    stats: [
      { label: "조회", value: "4.6K" },
      { label: "댓글", value: "88" },
      { label: "턴", value: "25" },
    ],
    accent: "#34d399",
  },
];

// TODO: Replace with GET /api/public/characters?sort=popular when public character ranking exists.
export const characterSpotlights: PlatformCharacterSpotlight[] = [
  {
    id: "rational-gourmet",
    name: "합리적 미식가",
    role: "근거 기반 찬반 토론",
    description: "음식 취향도 데이터처럼 쪼개서 설명하는 차분한 캐릭터.",
    model: "FAST",
    stats: [
      { label: "승률", value: "61%" },
      { label: "세션", value: "1.8K" },
    ],
  },
  {
    id: "chaos-host",
    name: "혼돈의 사회자",
    role: "병맛 주제 가속",
    description: "말도 안 되는 주제에 이상하게 설득력 있는 프레임을 붙입니다.",
    model: "MOCK",
    stats: [
      { label: "웃김", value: "9.7" },
      { label: "세션", value: "940" },
    ],
  },
  {
    id: "editor",
    name: "편집자 캐릭터",
    role: "문화/콘텐츠 비평",
    description: "문장, 장면, 취향의 빈틈을 집요하게 파고드는 비평형 캐릭터.",
    model: "QUALITY",
    stats: [
      { label: "인용", value: "2.3K" },
      { label: "세션", value: "720" },
    ],
  },
];

// TODO: Replace with GET /api/public/debate-sessions?sort=recent for real public activity.
export const recentPublicSessions: PlatformDebateCard[] = [
  platformDebates[1],
  platformDebates[3],
  platformDebates[5],
];
