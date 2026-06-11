import { createSeedMockState } from "@/server/mock-data/fixtures";
import { MockApiStore } from "@/server/mock-data/store";
import { describe, expect, it } from "vitest";

const persona = {
  identity: "균형 잡힌 판정관",
  debateRole: "중재자",
  coreValues: ["균형"],
  expertise: [],
  defaultStance: "양쪽 주장의 기준을 정리한다.",
  evidenceStyle: "근거와 논리를 우선한다.",
  debateBehavior: [],
  voiceStyle: {
    tone: "차분함",
    sentenceLength: "중간",
    rhetoricalStyle: "요약과 조율 중심",
    signaturePhrases: [],
  },
  boundaries: {
    mustDo: ["상대 주장을 먼저 요약한다."],
    mustNotDo: ["인신공격하지 않는다."],
  },
  exampleLines: [],
};

function authenticatedStore() {
  const store = new MockApiStore(createSeedMockState());
  const auth = store.login({
    email: "demo@example.com",
    password: "password123",
  });
  return { store, accessToken: auth.accessToken };
}

describe("MockApiStore", () => {
  it("filters and paginates public catalog data", () => {
    const store = new MockApiStore(createSeedMockState());
    const categories = store.listCategories("DEBATE");
    const debates = store.listPublicDebates(
      new URLSearchParams({ category: "food", page: "0", size: "1" }),
    );
    const characters = store.listPublicCharacters(
      new URLSearchParams({ query: "테크", page: "0", size: "20" }),
    );

    expect(categories.map((category) => category.slug)).toContain("food");
    expect(debates.items).toHaveLength(1);
    expect(debates.items?.[0]?.topicTitle).toBe("부먹 vs 찍먹");
    expect(debates.totalElements).toBe(1);
    expect(characters.items?.[0]?.name).toBe("테크 낙관론자");
  });

  it("creates owned characters and returns them through the workspace path", () => {
    const { store, accessToken } = authenticatedStore();

    const created = store.createCharacter(accessToken, {
      name: "균형 잡힌 판정관",
      category: "utility",
      description: "양쪽 주장의 기준을 정리합니다.",
      persona,
      visibility: "PRIVATE",
    });
    const owned = store.listCharactersForOwner(accessToken, 1);

    expect(created.id).toBeGreaterThan(1000);
    expect(created.category?.slug).toBe("utility");
    expect(owned.map((character) => character.name)).toContain(
      "균형 잡힌 판정관",
    );
  });

  it("runs debate lifecycle and alternates participants by request order", () => {
    const { store, accessToken } = authenticatedStore();
    const created = store.createDebateSession(accessToken, {
      topic: {
        title: "샘플 토론",
        description: "mock lifecycle 확인",
        category: "tech",
      },
      format: "PROS_AND_CONS",
      visibility: "PUBLIC",
      maxRounds: 1,
      maxTurnLength: 600,
      participants: [
        { characterId: 101, model: "FAST" },
        { characterId: 102, model: "QUALITY" },
      ],
    });

    expect(created.status).toBe("CREATED");
    expect(store.startSession(accessToken, created.id ?? 0).status).toBe("RUNNING");

    const first = store.generateTurn(accessToken, created.id ?? 0);
    const second = store.generateTurn(accessToken, created.id ?? 0);
    const turns = store.listAuthenticatedTurns(accessToken, created.id ?? 0);
    const publicPage = store.listPublicDebates(
      new URLSearchParams({ query: "샘플", page: "0", size: "20" }),
    );

    expect(first.participantId).toBe(created.participants?.[0]?.id);
    expect(second.participantId).toBe(created.participants?.[1]?.id);
    expect(turns.map((turn) => turn.participantModel)).toEqual([
      "FAST",
      "QUALITY",
    ]);
    expect(publicPage.items?.[0]?.status).toBe("COMPLETED");
  });
});
