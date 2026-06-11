import { WorkspaceApp } from "@/components/WorkspaceApp";
import { fireEvent, render, screen } from "@testing-library/react";
import { SWRConfig } from "swr";
import { afterEach, describe, expect, it, vi } from "vitest";

const push = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push,
    refresh: vi.fn(),
  }),
}));

function renderWorkspace() {
  return render(
    <SWRConfig value={{ provider: () => new Map(), dedupingInterval: 0 }}>
      <WorkspaceApp />
    </SWRConfig>,
  );
}

describe("WorkspaceApp", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    push.mockClear();
  });

  it("shows the search-first public catalog before authentication and routes creation CTA to login", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input);
        if (url === "/api/workspace") {
          return Promise.resolve(
            Response.json(
              { code: "UNAUTHORIZED", message: "Authentication is required" },
              { status: 401 },
            ),
          );
        }
        if (url.includes("/api/public/categories?scope=DEBATE")) {
          return Promise.resolve(
            Response.json([
              { id: 1, scope: "DEBATE", slug: "food", name: "음식" },
            ]),
          );
        }
        if (url.includes("/api/public/categories?scope=CHARACTER")) {
          return Promise.resolve(
            Response.json([
              { id: 2, scope: "CHARACTER", slug: "expert", name: "전문가" },
            ]),
          );
        }
        if (url.startsWith("/api/public/debate-sessions")) {
          return Promise.resolve(
            Response.json({
              items: [
                {
                  id: 7,
                  topicTitle: "부먹 vs 찍먹",
                  topicDescription: "어느 방식이 더 나은가?",
                  topicCategory: "food",
                  category: { id: 1, scope: "DEBATE", slug: "food", name: "음식" },
                  status: "COMPLETED",
                  format: "PROS_AND_CONS",
                  currentRound: 5,
                  maxRounds: 5,
                  endedAt: "2026-06-11T10:00:00",
                  participants: [{ id: 1, name: "합리적 미식가", position: 0 }],
                },
              ],
              page: 0,
              size: 20,
              totalElements: 1,
              totalPages: 1,
              hasNext: false,
            }),
          );
        }
        if (url.startsWith("/api/public/characters")) {
          return Promise.resolve(
            Response.json({
              items: [],
              page: 0,
              size: 20,
              totalElements: 0,
              totalPages: 0,
              hasNext: false,
            }),
          );
        }
        return Promise.resolve(Response.json({}));
      }),
    );

    renderWorkspace();

    expect(screen.getByRole("tab", { name: "공개 토론" })).toBeVisible();
    expect(screen.getByLabelText("검색어")).toBeVisible();
    expect(await screen.findByText("부먹 vs 찍먹")).toBeVisible();
    expect(await screen.findByRole("button", { name: "음식" })).toBeVisible();
    expect(await screen.findByLabelText("부먹 vs 찍먹 썸네일")).toBeVisible();
    expect(await screen.findByText("합리적 미식가")).toBeVisible();
    expect(
      screen.queryByText("완료된 AI 토론과 공개 캐릭터를 바로 탐색합니다."),
    ).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "만들기" }));

    expect(push).toHaveBeenCalledWith("/login");
  });
});
