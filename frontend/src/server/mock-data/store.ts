import {
  createSeedMockState,
  type AuthTokensResponse,
  type CategorySummaryResponse,
  type CharacterResponse,
  type CompleteDebateSessionResponse,
  type CreateCharacterRequest,
  type CreateDebateSessionRequest,
  type DebateSessionResponse,
  type DebateTurnResponse,
  type GenerateTurnResponse,
  type LoginRequest,
  type MockDebateTurn,
  type MockState,
  type PublicCharacterPageResponse,
  type PublicDebateSessionPageResponse,
  type RefreshTokenRequest,
  type SignupRequest,
  type StartDebateSessionResponse,
  type UserResponse,
} from "@/server/mock-data/fixtures";

export class MockStoreError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

export class MockApiStore {
  constructor(private state: MockState = createSeedMockState()) {}

  listCategories(scope: string | null): CategorySummaryResponse[] {
    if (scope !== "DEBATE" && scope !== "CHARACTER") {
      throw new MockStoreError(
        404,
        "CATEGORY_NOT_FOUND",
        "카테고리를 찾을 수 없습니다.",
      );
    }

    return cloneJson(
      this.state.categories.filter((category) => category.scope === scope),
    );
  }

  listPublicDebates(
    params: URLSearchParams,
  ): PublicDebateSessionPageResponse {
    const categorySlug = params.get("category")?.trim() || undefined;
    if (categorySlug) {
      this.requireCategory("DEBATE", categorySlug);
    }

    const query = normalize(params.get("query"));
    const items = this.state.debateSessions
      .filter(
        (session) =>
          session.visibility === "PUBLIC" && session.status === "COMPLETED",
      )
      .filter((session) =>
        categorySlug ? session.category?.slug === categorySlug : true,
      )
      .filter((session) => {
        if (!query) {
          return true;
        }
        return normalize(
          [
            session.topicTitle,
            session.topicDescription,
            session.category?.name,
            ...(session.participants ?? []).map((participant) => participant.name),
          ].join(" "),
        ).includes(query);
      })
      .toSorted((left, right) =>
        String(right.createdAt ?? "").localeCompare(String(left.createdAt ?? "")),
      );

    return paginate(items, params);
  }

  getPublicDebate(sessionId: number): DebateSessionResponse {
    const session = this.findSession(sessionId);
    if (session.visibility !== "PUBLIC" || session.status !== "COMPLETED") {
      throw new MockStoreError(
        404,
        "DEBATE_SESSION_NOT_FOUND",
        "토론 세션을 찾을 수 없습니다.",
      );
    }

    return cloneJson(session);
  }

  listPublicDebateTurns(sessionId: number): DebateTurnResponse[] {
    this.getPublicDebate(sessionId);
    return this.listTurnsForSession(sessionId);
  }

  listPublicCharacters(
    params: URLSearchParams,
  ): PublicCharacterPageResponse {
    const categorySlug = params.get("category")?.trim() || undefined;
    if (categorySlug) {
      this.requireCategory("CHARACTER", categorySlug);
    }

    const query = normalize(params.get("query"));
    const items = this.state.characters
      .filter((character) => character.visibility === "PUBLIC")
      .filter((character) =>
        categorySlug ? character.category?.slug === categorySlug : true,
      )
      .filter((character) => {
        if (!query) {
          return true;
        }
        return normalize(
          [character.name, character.description, character.category?.name].join(
            " ",
          ),
        ).includes(query);
      })
      .toSorted((left, right) =>
        String(right.createdAt ?? "").localeCompare(String(left.createdAt ?? "")),
      );

    return paginate(items, params);
  }

  getPublicCharacter(characterId: number): CharacterResponse {
    const character = this.state.characters.find(
      (item) => item.id === characterId && item.visibility === "PUBLIC",
    );
    if (!character) {
      throw new MockStoreError(
        404,
        "CHARACTER_NOT_FOUND",
        "캐릭터를 찾을 수 없습니다.",
      );
    }

    return cloneJson(character);
  }

  login(payload: LoginRequest): AuthTokensResponse {
    const record = this.state.users.find(
      (candidate) => candidate.user.email === payload.email,
    );
    if (!record || record.password !== payload.password) {
      throw new MockStoreError(
        401,
        "INVALID_CREDENTIALS",
        "이메일 또는 비밀번호가 올바르지 않습니다.",
      );
    }

    return this.issueTokens(record.user);
  }

  signup(payload: SignupRequest): AuthTokensResponse {
    if (
      this.state.users.some(
        (record) =>
          record.user.email?.toLowerCase() === payload.email.toLowerCase(),
      )
    ) {
      throw new MockStoreError(
        409,
        "EMAIL_ALREADY_EXISTS",
        "이미 가입된 이메일입니다.",
      );
    }

    const user: UserResponse = {
      id: this.state.nextUserId,
      email: payload.email,
      nickname: payload.nickname,
      createdAt: now(),
    };
    this.state.nextUserId += 1;
    this.state.users.push({ user, password: payload.password });
    return this.issueTokens(user);
  }

  refresh(payload: RefreshTokenRequest): AuthTokensResponse {
    const sessionIndex = this.state.authSessions.findIndex(
      (session) => session.refreshToken === payload.refreshToken,
    );
    if (sessionIndex < 0) {
      throw new MockStoreError(401, "INVALID_TOKEN", "Refresh token이 없습니다.");
    }

    const [session] = this.state.authSessions.splice(sessionIndex, 1);
    const user = this.findUser(session.userId);
    return this.issueTokens(user);
  }

  logout(refreshToken: string | undefined): void {
    if (!refreshToken) {
      return;
    }

    this.state.authSessions = this.state.authSessions.filter(
      (session) => session.refreshToken !== refreshToken,
    );
  }

  getCurrentUser(accessToken: string | undefined): UserResponse {
    return cloneJson(this.requireUser(accessToken));
  }

  listCharactersForOwner(
    accessToken: string | undefined,
    ownerId: number | undefined,
  ): CharacterResponse[] {
    const user = this.requireUser(accessToken);
    if (!ownerId || ownerId !== user.id) {
      throw new MockStoreError(
        404,
        "CHARACTER_NOT_FOUND",
        "캐릭터를 찾을 수 없습니다.",
      );
    }

    return cloneJson(
      this.state.characters.filter((character) => character.ownerId === user.id),
    );
  }

  createCharacter(
    accessToken: string | undefined,
    payload: CreateCharacterRequest,
  ): CharacterResponse {
    const user = this.requireUser(accessToken);
    if (!payload.name.trim()) {
      throw new MockStoreError(
        400,
        "INVALID_DEBATE_RULE",
        "캐릭터 이름을 입력해 주세요.",
      );
    }

    const category = this.requireCategory(
      "CHARACTER",
      payload.category?.trim() || "other",
    );
    const timestamp = now();
    const character: CharacterResponse = {
      id: this.state.nextCharacterId,
      ownerId: user.id,
      name: payload.name.trim(),
      description: payload.description,
      category,
      persona: payload.persona,
      visibility: payload.visibility ?? "PRIVATE",
      createdAt: timestamp,
      updatedAt: timestamp,
    };

    this.state.nextCharacterId += 1;
    this.state.characters.push(character);
    return cloneJson(character);
  }

  createDebateSession(
    accessToken: string | undefined,
    payload: CreateDebateSessionRequest,
  ): DebateSessionResponse {
    const user = this.requireUser(accessToken);
    if (payload.participants.length !== 2) {
      throw new MockStoreError(
        400,
        "INVALID_PARTICIPANT_COUNT",
        "토론 참가자는 정확히 2명이어야 합니다.",
      );
    }

    const category = this.requireCategory(
      "DEBATE",
      payload.topic.category?.trim() || "other",
    );
    const participants = payload.participants.map((participant, index) => {
      const character = this.requireAccessibleCharacter(
        user.id,
        participant.characterId,
      );
      const response: NonNullable<DebateSessionResponse["participants"]>[number] = {
        id: this.state.nextParticipantId,
        sourceCharacterId: character.id,
        position: index,
        model: participant.model,
        name: character.name,
        description: character.description,
        persona: character.persona,
      };
      this.state.nextParticipantId += 1;
      return response;
    });

    const session: DebateSessionResponse = {
      id: this.state.nextDebateSessionId,
      ownerId: user.id,
      topicTitle: payload.topic.title,
      topicDescription: payload.topic.description,
      topicCategory: category.slug,
      category,
      status: "CREATED",
      visibility: payload.visibility ?? "PRIVATE",
      format: payload.format,
      maxRounds: payload.maxRounds ?? 5,
      currentRound: 0,
      maxTurnLength: payload.maxTurnLength,
      participants,
      createdAt: now(),
    };

    this.state.nextDebateSessionId += 1;
    this.state.debateSessions.push(session);
    return cloneJson(session);
  }

  startSession(
    accessToken: string | undefined,
    sessionId: number,
  ): StartDebateSessionResponse {
    const session = this.requireOwnedSession(accessToken, sessionId);
    if (session.status !== "CREATED") {
      throw new MockStoreError(
        409,
        "INVALID_SESSION_STATE",
        "CREATED 상태의 세션만 시작할 수 있습니다.",
      );
    }

    session.status = "RUNNING";
    session.startedAt = now();
    return {
      id: session.id,
      status: session.status,
      startedAt: session.startedAt,
    };
  }

  completeSession(
    accessToken: string | undefined,
    sessionId: number,
  ): CompleteDebateSessionResponse {
    const session = this.requireOwnedSession(accessToken, sessionId);
    if (session.status !== "RUNNING") {
      throw new MockStoreError(
        409,
        "INVALID_SESSION_STATE",
        "RUNNING 상태의 세션만 완료할 수 있습니다.",
      );
    }

    session.status = "COMPLETED";
    session.endedAt = now();
    session.currentRound = session.maxRounds ?? session.currentRound;
    return {
      id: session.id,
      status: session.status,
      endedAt: session.endedAt,
    };
  }

  generateTurn(
    accessToken: string | undefined,
    sessionId: number,
  ): GenerateTurnResponse {
    const session = this.requireOwnedSession(accessToken, sessionId);
    if (session.status !== "RUNNING") {
      throw new MockStoreError(
        409,
        "INVALID_SESSION_STATE",
        "RUNNING 상태의 세션만 발화를 생성할 수 있습니다.",
      );
    }

    const participants = (session.participants ?? []).toSorted(
      (left, right) => (left.position ?? 0) - (right.position ?? 0),
    );
    if (participants.length !== 2) {
      throw new MockStoreError(
        400,
        "INVALID_PARTICIPANT_COUNT",
        "토론 참가자는 정확히 2명이어야 합니다.",
      );
    }

    const previousTurns = this.state.debateTurns.filter(
      (turn) => turn.sessionId === session.id,
    );
    const maxTurns = (session.maxRounds ?? 5) * participants.length;
    if (previousTurns.length >= maxTurns) {
      throw new MockStoreError(
        409,
        "INVALID_SESSION_STATE",
        "이미 최대 턴 수에 도달했습니다.",
      );
    }

    const turnIndex = previousTurns.length + 1;
    const round = Math.floor((turnIndex - 1) / participants.length) + 1;
    const participant = participants[(turnIndex - 1) % participants.length];
    const participantModel = participant.model ?? "MOCK";
    const timestamp = now();
    const turn: MockDebateTurn = {
      id: this.state.nextTurnId,
      sessionId: session.id,
      participantId: participant.id,
      participantModel,
      round,
      turnIndex,
      type: nextTurnType(turnIndex, maxTurns),
      status: "COMPLETED",
      content: makeTurnContent(session, participant.name ?? "참가자", round),
      modelName: `mock-${participantModel.toLowerCase()}`,
      inputTokens: 0,
      outputTokens: 0,
      createdAt: timestamp,
    };

    this.state.nextTurnId += 1;
    this.state.debateTurns.push(turn);

    if (turnIndex % participants.length === 0) {
      session.currentRound = round;
    }
    if (turnIndex >= maxTurns) {
      session.status = "COMPLETED";
      session.endedAt = timestamp;
      session.currentRound = session.maxRounds;
    }

    return toGenerateTurnResponse(turn);
  }

  listAuthenticatedTurns(
    accessToken: string | undefined,
    sessionId: number,
  ): DebateTurnResponse[] {
    this.requireOwnedSession(accessToken, sessionId);
    return this.listTurnsForSession(sessionId);
  }

  private issueTokens(user: UserResponse): AuthTokensResponse {
    if (!user.id) {
      throw new MockStoreError(401, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
    }

    const tokenId = this.state.nextTokenId;
    this.state.nextTokenId += 1;
    const accessToken = `mock-access-${user.id}-${tokenId}`;
    const refreshToken = `mock-refresh-${user.id}-${tokenId}`;
    this.state.authSessions.push({ userId: user.id, accessToken, refreshToken });

    return {
      user: cloneJson(user),
      accessToken,
      refreshToken,
      tokenType: "Bearer",
      expiresIn: 900,
    };
  }

  private requireUser(accessToken: string | undefined): UserResponse {
    if (!accessToken) {
      throw new MockStoreError(401, "UNAUTHORIZED", "로그인이 필요합니다.");
    }

    const session = this.state.authSessions.find(
      (candidate) => candidate.accessToken === accessToken,
    );
    if (!session) {
      throw new MockStoreError(401, "INVALID_TOKEN", "Access token이 유효하지 않습니다.");
    }

    return this.findUser(session.userId);
  }

  private findUser(userId: number): UserResponse {
    const record = this.state.users.find((candidate) => candidate.user.id === userId);
    if (!record) {
      throw new MockStoreError(404, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
    }

    return record.user;
  }

  private requireOwnedSession(
    accessToken: string | undefined,
    sessionId: number,
  ): DebateSessionResponse {
    const user = this.requireUser(accessToken);
    const session = this.findSession(sessionId);
    if (session.ownerId !== user.id) {
      throw new MockStoreError(
        404,
        "DEBATE_SESSION_NOT_FOUND",
        "토론 세션을 찾을 수 없습니다.",
      );
    }

    return session;
  }

  private findSession(sessionId: number): DebateSessionResponse {
    const session = this.state.debateSessions.find((item) => item.id === sessionId);
    if (!session) {
      throw new MockStoreError(
        404,
        "DEBATE_SESSION_NOT_FOUND",
        "토론 세션을 찾을 수 없습니다.",
      );
    }

    return session;
  }

  private requireAccessibleCharacter(
    userId: number | undefined,
    characterId: number,
  ): CharacterResponse {
    const character = this.state.characters.find((item) => item.id === characterId);
    if (
      !character ||
      (character.ownerId !== userId && character.visibility !== "PUBLIC")
    ) {
      throw new MockStoreError(
        404,
        "CHARACTER_NOT_FOUND",
        "캐릭터를 찾을 수 없습니다.",
      );
    }

    return character;
  }

  private requireCategory(
    scope: NonNullable<CategorySummaryResponse["scope"]>,
    slug: string,
  ): CategorySummaryResponse {
    const category = this.state.categories.find(
      (candidate) => candidate.scope === scope && candidate.slug === slug,
    );
    if (!category) {
      throw new MockStoreError(
        404,
        "CATEGORY_NOT_FOUND",
        "카테고리를 찾을 수 없습니다.",
      );
    }

    return cloneJson(category);
  }

  private listTurnsForSession(sessionId: number): DebateTurnResponse[] {
    return cloneJson(
      this.state.debateTurns
        .filter((turn) => turn.sessionId === sessionId)
        .toSorted((left, right) => (left.turnIndex ?? 0) - (right.turnIndex ?? 0))
        .map(toDebateTurnResponse),
    );
  }
}

const globalForMock = globalThis as typeof globalThis & {
  __aichatMockStore?: MockApiStore;
};

export function getMockStore(): MockApiStore {
  globalForMock.__aichatMockStore ??= new MockApiStore();
  return globalForMock.__aichatMockStore;
}

export function resetMockStoreForTest(state: MockState = createSeedMockState()) {
  globalForMock.__aichatMockStore = new MockApiStore(state);
}

function paginate<T>(
  items: T[],
  params: URLSearchParams,
): {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
} {
  const page = parseInteger(params.get("page"), 0, 0, Number.MAX_SAFE_INTEGER);
  const size = parseInteger(params.get("size"), 20, 1, 100);
  const totalElements = items.length;
  const totalPages = Math.ceil(totalElements / size);
  const start = page * size;

  return {
    items: cloneJson(items.slice(start, start + size)),
    page,
    size,
    totalElements,
    totalPages,
    hasNext: page + 1 < totalPages,
  };
}

function parseInteger(
  value: string | null,
  fallback: number,
  minimum: number,
  maximum: number,
) {
  if (value === null) {
    return fallback;
  }

  const parsed = Number(value);
  if (!Number.isInteger(parsed)) {
    return fallback;
  }

  return Math.min(Math.max(parsed, minimum), maximum);
}

function toDebateTurnResponse(turn: MockDebateTurn): DebateTurnResponse {
  return {
    id: turn.id,
    participantId: turn.participantId,
    participantModel: turn.participantModel,
    round: turn.round,
    turnIndex: turn.turnIndex,
    type: turn.type,
    content: turn.content,
    createdAt: turn.createdAt,
  };
}

function toGenerateTurnResponse(turn: MockDebateTurn): GenerateTurnResponse {
  return {
    id: turn.id,
    sessionId: turn.sessionId,
    participantId: turn.participantId,
    round: turn.round,
    turnIndex: turn.turnIndex,
    type: turn.type,
    status: turn.status,
    content: turn.content,
    modelName: turn.modelName,
    inputTokens: turn.inputTokens,
    outputTokens: turn.outputTokens,
    createdAt: turn.createdAt,
  };
}

function nextTurnType(
  turnIndex: number,
  maxTurns: number,
): NonNullable<DebateTurnResponse["type"]> {
  if (turnIndex === maxTurns) {
    return "SUMMARY";
  }
  return turnIndex % 2 === 0 ? "REBUTTAL" : "ARGUMENT";
}

function makeTurnContent(
  session: DebateSessionResponse,
  participantName: string,
  round: number,
) {
  const title = session.topicTitle ?? "토론 주제";
  return `${participantName}은(는) '${title}'에 대해 ${round}라운드 핵심 근거를 제시합니다. 실제 백엔드 없이도 화면 흐름을 확인할 수 있는 개발용 응답입니다.`;
}

function normalize(value: string | null | undefined) {
  return value?.trim().toLowerCase() ?? "";
}

function now() {
  return new Date().toISOString();
}

function cloneJson<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}
