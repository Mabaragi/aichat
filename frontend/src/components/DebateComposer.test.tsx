import { DebateComposer } from "@/components/DebateComposer";
import type { Character } from "@/lib/api-types";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

const character: Character = {
  id: 10,
  ownerId: 1,
  name: "합리적 미식가",
  description: "차분하게 근거를 설명한다.",
  visibility: "PRIVATE",
};

describe("DebateComposer", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("allows the same character twice and preserves participant order", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      Response.json({
        id: 7,
        topicTitle: "부먹 vs 찍먹",
        topicDescription: "어느 방식이 더 나은가?",
        status: "CREATED",
        participants: [
          {
            id: 1,
            sourceCharacterId: 10,
            position: 0,
            name: "합리적 미식가",
            model: "FAST",
            personality: { rationality: 80 },
          },
          {
            id: 2,
            sourceCharacterId: 10,
            position: 1,
            name: "합리적 미식가",
            model: "QUALITY",
            speechStyle: { tone: "calm" },
          },
        ],
      }),
    );
    vi.stubGlobal("fetch", fetchMock);
    const { rerender } = render(<DebateComposer characters={[]} />);
    rerender(<DebateComposer characters={[character]} />);

    fireEvent.change(screen.getByLabelText("제목"), {
      target: { value: "부먹 vs 찍먹" },
    });
    fireEvent.change(screen.getByLabelText("설명"), {
      target: { value: "어느 방식이 더 나은가?" },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "토론 세션 생성" }).closest("form")!,
    );

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(JSON.parse(String(init.body))).toMatchObject({
      participants: [
        { characterId: 10, model: "FAST" },
        { characterId: 10, model: "QUALITY" },
      ],
    });
    expect(await screen.findByText("SESSION CREATED")).toBeInTheDocument();
    expect(screen.getByText("FAST / character #10")).toBeVisible();
    expect(screen.getByText("QUALITY / character #10")).toBeVisible();
  });
});
