import { handleCredentialAuth } from "@/server/auth-handler";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const authPayload = {
  user: {
    id: 1,
    email: "owner@example.com",
    nickname: "owner",
    createdAt: "2026-06-10T12:00:00",
  },
  accessToken: "access-secret",
  refreshToken: "refresh-secret",
  tokenType: "Bearer",
  expiresIn: 900,
};

function loginRequest(origin = "http://localhost") {
  return new Request("http://localhost/api/auth/login", {
    method: "POST",
    headers: {
      origin,
      "content-type": "application/json",
    },
    body: JSON.stringify({
      email: "owner@example.com",
      password: "password123",
    }),
  });
}

describe("credential auth BFF", () => {
  beforeEach(() => {
    process.env.BACKEND_BASE_URL = "http://backend:8080";
    process.env.AUTH_COOKIE_SECURE = "false";
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("keeps tokens out of JSON and stores them in HttpOnly cookies", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      Response.json(authPayload, {
        status: 200,
        headers: { "content-type": "application/json" },
      }),
    );
    vi.stubGlobal("fetch", fetchMock);

    const response = await handleCredentialAuth(
      loginRequest(),
      "/api/auth/login",
    );

    expect(await response.json()).toEqual({ user: authPayload.user });
    const cookies = response.headers.getSetCookie().join("; ");
    expect(cookies).toContain("aichat_access=access-secret");
    expect(cookies).toContain("aichat_refresh=refresh-secret");
    expect(cookies).toContain("HttpOnly");
    expect(cookies).toContain("SameSite=lax");
    expect(cookies).not.toContain("Secure");
    expect(fetchMock).toHaveBeenCalledWith(
      "http://backend:8080/api/auth/login",
      expect.objectContaining({ method: "POST", cache: "no-store" }),
    );
  });

  it("rejects a mutation from a different origin", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    const response = await handleCredentialAuth(
      loginRequest("http://different.example"),
      "/api/auth/login",
    );

    expect(response.status).toBe(403);
    expect(await response.json()).toMatchObject({ code: "INVALID_ORIGIN" });
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("preserves backend error status and body", async () => {
    const error = {
      code: "INVALID_CREDENTIALS",
      message: "Email or password is invalid",
    };
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(Response.json(error, { status: 401 })),
    );

    const response = await handleCredentialAuth(
      loginRequest(),
      "/api/auth/login",
    );

    expect(response.status).toBe(401);
    expect(await response.json()).toEqual(error);
  });

  it("normalizes connection failures", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("offline")));

    const response = await handleCredentialAuth(
      loginRequest(),
      "/api/auth/login",
    );

    expect(response.status).toBe(502);
    expect(await response.json()).toMatchObject({
      code: "UPSTREAM_UNAVAILABLE",
    });
  });
});
