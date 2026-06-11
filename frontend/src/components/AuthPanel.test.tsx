import { AuthPanel } from "@/components/AuthPanel";
import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

describe("AuthPanel", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("validates the signup password before requesting the BFF", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    render(<AuthPanel onAuthenticated={vi.fn()} />);

    fireEvent.click(screen.getByRole("tab", { name: "회원가입" }));
    fireEvent.change(screen.getByLabelText("이메일"), {
      target: { value: "owner@example.com" },
    });
    fireEvent.change(screen.getByLabelText("닉네임"), {
      target: { value: "owner" },
    });
    fireEvent.change(screen.getByLabelText("비밀번호"), {
      target: { value: "short" },
    });
    fireEvent.submit(
      screen.getByRole("button", { name: "가입" }).closest("form")!,
    );

    expect(await screen.findByText("비밀번호는 8자 이상이어야 합니다.")).toBeVisible();
    expect(fetchMock).not.toHaveBeenCalled();
  });
});
