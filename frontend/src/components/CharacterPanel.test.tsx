import { CharacterPanel } from "@/components/CharacterPanel";
import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

const categories = [
  { id: 1, scope: "CHARACTER" as const, slug: "other", name: "기타" },
];

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
        categories={categories}
        onCreated={vi.fn()}
        onLogout={vi.fn()}
      />,
    );

    fireEvent.change(screen.getByLabelText("이름"), {
      target: { value: "테스트 토론가" },
    });
    fireEvent.change(screen.getByLabelText("성격 JSON"), {
      target: { value: "[]" },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "캐릭터 저장" }).closest("form")!,
    );

    expect(await screen.findByText("성격 항목은 JSON object여야 합니다.")).toBeVisible();
    expect(fetchMock).not.toHaveBeenCalled();
  });
});
