import { POST as logout } from "@/app/api/auth/logout/route";
import { POST as refresh } from "@/app/api/auth/refresh/route";
import { cookies } from "next/headers";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("next/headers", () => ({
  cookies: vi.fn(),
}));

function mutationRequest(path: string) {
  return new Request(`http://localhost${path}`, {
    method: "POST",
    headers: {
      origin: "http://localhost",
      "content-type": "application/json",
    },
    body: "{}",
  });
}

describe("auth Route Handlers", () => {
  beforeEach(() => {
    process.env.BACKEND_BASE_URL = "http://backend:8080";
    process.env.AUTH_COOKIE_SECURE = "false";
    vi.mocked(cookies).mockResolvedValue({
      get: (name: string) =>
        name === "aichat_refresh" ? { name, value: "old-refresh" } : undefined,
    } as Awaited<ReturnType<typeof cookies>>);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.clearAllMocks();
  });

  it("rotates both cookies without returning tokens to the browser", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      Response.json({
        user: { id: 1, email: "owner@example.com", nickname: "owner" },
        accessToken: "new-access",
        refreshToken: "new-refresh",
        tokenType: "Bearer",
        expiresIn: 900,
      }),
    );
    vi.stubGlobal("fetch", fetchMock);

    const response = await refresh(mutationRequest("/api/auth/refresh"));

    expect(await response.json()).toEqual({
      user: { id: 1, email: "owner@example.com", nickname: "owner" },
    });
    const cookieHeaders = response.headers.getSetCookie().join("; ");
    expect(cookieHeaders).toContain("aichat_access=new-access");
    expect(cookieHeaders).toContain("aichat_refresh=new-refresh");
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(JSON.parse(String(init.body))).toEqual({
      refreshToken: "old-refresh",
    });
  });

  it("expires local cookies when logout succeeds", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response(null, { status: 204 })),
    );

    const response = await logout(mutationRequest("/api/auth/logout"));

    expect(response.status).toBe(204);
    const cookieHeaders = response.headers.getSetCookie().join("; ");
    expect(cookieHeaders.match(/Max-Age=0/g)).toHaveLength(2);
  });
});
