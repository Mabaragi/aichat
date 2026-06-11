import { CharacterPanel } from "@/components/CharacterPanel";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

const categories = [
  { id: 1, scope: "CHARACTER" as const, slug: "other", name: "기타" },
];

describe("CharacterPanel", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
  });

  it("renders the persona builder flow", () => {
    render(
      <CharacterPanel
        user={{ id: 1, email: "owner@example.com", nickname: "owner" }}
        characters={[]}
        categories={categories}
        onCreated={vi.fn()}
        onLogout={vi.fn()}
      />,
    );

    expect(screen.getByLabelText("페르소나 미리보기")).toBeInTheDocument();
    expect(screen.getByLabelText("어떤 캐릭터인가요?")).toBeInTheDocument();
    expect(screen.getByRole("radio", { name: /데이터 분석가/ })).toBeChecked();
    expect(screen.getByRole("slider", { name: /입장 수정/ })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "더 날카롭게" })).toBeInTheDocument();
  });

  it("builds a structured persona payload from cards and controls", async () => {
    const onCreated = vi.fn();
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          id: 10,
          ownerId: 1,
          name: "테스트 토론가",
          persona: {
            identity: "실행 비용을 먼저 보는 검증자",
            debateRole: "회의적 반대자",
            coreValues: ["검증", "위험 관리"],
            expertise: ["반례 탐색", "리스크 분석"],
            defaultStance: "좋은 의도보다 실패 조건과 부작용을 먼저 본다.",
            evidenceStyle: "실행 비용, 운영 부담, 부작용을 중심으로 판단한다.",
            debateBehavior: ["결론보다 전제의 타당성을 먼저 검토한다."],
            voiceStyle: {
              tone: "직설적",
              sentenceLength: "중간",
              rhetoricalStyle: "전제 분해와 반례 제시 중심",
              signaturePhrases: [],
            },
            boundaries: {
              mustDo: ["상대 주장을 먼저 요약한다.", "불확실한 사실은 단정하지 않는다."],
              mustNotDo: ["인신공격하지 않는다."],
            },
            exampleLines: [],
          },
          visibility: "PRIVATE",
        }),
        { status: 201, headers: { "content-type": "application/json" } },
      ),
    );
    vi.stubGlobal("fetch", fetchMock);
    render(
      <CharacterPanel
        user={{ id: 1, email: "owner@example.com", nickname: "owner" }}
        characters={[]}
        categories={categories}
        onCreated={onCreated}
        onLogout={vi.fn()}
      />,
    );

    fireEvent.change(screen.getByLabelText("이름"), {
      target: { value: "테스트 토론가" },
    });
    fireEvent.change(screen.getByLabelText("어떤 캐릭터인가요?"), {
      target: { value: "실행 비용을 먼저 보는 검증자" },
    });
    fireEvent.click(screen.getByRole("radio", { name: /회의적 반대자/ }));
    fireEvent.click(screen.getByRole("radio", { name: /현실 비용/ }));
    fireEvent.click(screen.getByRole("radio", { name: /전제 공격/ }));
    fireEvent.click(screen.getByRole("radio", { name: /직설적/ }));
    fireEvent.change(screen.getByRole("slider", { name: /반박 압력/ }), {
      target: { value: "5" },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "캐릭터 저장" }).closest("form")!,
    );

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));

    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    const payload = JSON.parse(String(init.body));
    expect(payload.personality).toBeUndefined();
    expect(payload.speechStyle).toBeUndefined();
    expect(payload.persona).toMatchObject({
      identity: "실행 비용을 먼저 보는 검증자",
      debateRole: "회의적 반대자",
      coreValues: ["검증", "위험 관리"],
      evidenceStyle: expect.stringContaining("실행 비용"),
      debateBehavior: expect.arrayContaining([
        "결론보다 전제의 타당성을 먼저 검토한다.",
        "상대 주장의 핵심 전제를 강하게 압박한다.",
      ]),
      voiceStyle: {
        tone: "직설적 날카로움",
        sentenceLength: "중간",
      },
    });
    expect(payload.persona.boundaries.mustNotDo).toContain("인신공격하지 않는다.");
    expect(onCreated).toHaveBeenCalled();
  });

  it("applies quick tuning buttons to the builder", () => {
    render(
      <CharacterPanel
        user={{ id: 1, email: "owner@example.com", nickname: "owner" }}
        characters={[]}
        categories={categories}
        onCreated={vi.fn()}
        onLogout={vi.fn()}
      />,
    );

    fireEvent.click(screen.getByRole("button", { name: "더 날카롭게" }));

    expect(
      (screen.getByRole("radio", { name: /날카로운 질문/ }) as HTMLInputElement)
        .checked,
    ).toBe(true);
  });
});
