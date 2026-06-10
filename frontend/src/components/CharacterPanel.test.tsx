import { CharacterPanel } from "@/components/CharacterPanel";
import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

describe("CharacterPanel", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("rejects array JSON before sending a character mutation", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    render(
      <CharacterPanel
        user={{ id: 1, email: "owner@example.com", nickname: "owner" }}
        characters={[]}
        onCreated={vi.fn()}
        onLogout={vi.fn()}
      />,
    );

    fireEvent.click(screen.getByRole("button", { name: "새 캐릭터 만들기" }));
    fireEvent.change(screen.getByLabelText("이름"), {
      target: { value: "테스터" },
    });
    const personality = screen.getByLabelText("성격 JSON");
    fireEvent.change(personality, { target: { value: "[]" } });
    fireEvent.submit(
      screen.getByRole("button", { name: "캐릭터 저장" }).closest("form")!,
    );

    expect(await screen.findByText("성격은 JSON object여야 합니다.")).toBeVisible();
    expect(fetchMock).not.toHaveBeenCalled();
  });
});
