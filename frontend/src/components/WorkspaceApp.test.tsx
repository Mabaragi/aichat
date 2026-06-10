import { WorkspaceApp } from "@/components/WorkspaceApp";
import { fireEvent, render, screen } from "@testing-library/react";
import { SWRConfig } from "swr";
import { afterEach, describe, expect, it, vi } from "vitest";

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
  });

  it("shows the public catalog before authentication and opens auth from creation CTA", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        Response.json(
          { code: "UNAUTHORIZED", message: "Authentication is required" },
          { status: 401 },
        ),
      ),
    );

    renderWorkspace();

    expect(screen.getAllByText("탕수육은 부먹인가 찍먹인가")[0]).toBeVisible();
    expect(screen.getByRole("button", { name: "음식" })).toBeVisible();

    fireEvent.click(screen.getByRole("button", { name: "무료로 둘러보고 시작" }));

    expect(await screen.findByRole("dialog")).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "로그인" })).toBeVisible();
  });
});
