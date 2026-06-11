import { POST as login } from "@/app/api/auth/login/route";
import { POST as createCharacter } from "@/app/api/characters/route";
import { GET as publicCategories } from "@/app/api/public/categories/route";
import { GET as workspace } from "@/app/api/workspace/route";
import { resetMockStoreForTest } from "@/server/mock-data/store";
import { cookies } from "next/headers";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("next/headers", () => ({
  cookies: vi.fn(),
}));

function mutationRequest(path: string, body: unknown, origin = "http://localhost") {
  return new Request(`http://localhost${path}`, {
    method: "POST",
    headers: {
      origin,
      "content-type": "application/json",
    },
    body: JSON.stringify(body),
  });
}

function cookieStore(values: Record<string, string>) {
  return {
    get: (name: string) =>
      values[name] ? { name, value: values[name] } : undefined,
  } as Awaited<ReturnType<typeof cookies>>;
}

function extractCookieValue(response: Response, name: string) {
  const header = response.headers.getSetCookie().join("; ");
  const match = header.match(new RegExp(`${name}=([^;]+)`));
  return match?.[1];
}

describe("mock mode Route Handlers", () => {
  beforeEach(() => {
    process.env.FRONTEND_API_MODE = "mock";
    process.env.AUTH_COOKIE_SECURE = "false";
    delete process.env.BACKEND_BASE_URL;
    resetMockStoreForTest();
    vi.mocked(cookies).mockResolvedValue(cookieStore({}));
  });

  afterEach(() => {
    delete process.env.FRONTEND_API_MODE;
    delete process.env.AUTH_COOKIE_SECURE;
    delete process.env.BACKEND_BASE_URL;
    vi.unstubAllGlobals();
    vi.clearAllMocks();
  });

  it("serves public data without calling the backend", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    const response = await publicCategories(
      new Request("http://localhost/api/public/categories?scope=DEBATE"),
    );

    expect(response.status).toBe(200);
    expect(await response.json()).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ scope: "DEBATE", slug: "food" }),
      ]),
    );
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("stores mock auth tokens in HttpOnly cookies without exposing them in JSON", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    const response = await login(
      mutationRequest("/api/auth/login", {
        email: "demo@example.com",
        password: "password123",
      }),
    );

    expect(response.status).toBe(200);
    expect(await response.json()).toEqual({
      user: expect.objectContaining({ email: "demo@example.com" }),
    });
    const cookieHeaders = response.headers.getSetCookie().join("; ");
    expect(cookieHeaders).toContain("aichat_access=mock-access-1-1");
    expect(cookieHeaders).toContain("aichat_refresh=mock-refresh-1-1");
    expect(cookieHeaders).toContain("HttpOnly");
    expect(cookieHeaders).not.toContain("Secure");
    expect(cookieHeaders).not.toContain("accessToken");
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("serves workspace and mutations from the same mock session", async () => {
    const loginResponse = await login(
      mutationRequest("/api/auth/login", {
        email: "demo@example.com",
        password: "password123",
      }),
    );
    const accessToken = extractCookieValue(loginResponse, "aichat_access");
    expect(accessToken).toBeDefined();
    vi.mocked(cookies).mockResolvedValue(
      cookieStore({ aichat_access: accessToken ?? "" }),
    );

    const createdResponse = await createCharacter(
      mutationRequest("/api/characters", {
        name: "새 캐릭터",
        category: "creator",
        visibility: "PRIVATE",
      }),
    );
    expect(createdResponse.status).toBe(201);

    const workspaceResponse = await workspace();
    expect(workspaceResponse.status).toBe(200);
    expect(await workspaceResponse.json()).toMatchObject({
      user: { email: "demo@example.com" },
      characters: expect.arrayContaining([
        expect.objectContaining({ name: "새 캐릭터" }),
      ]),
    });
  });

  it("keeps same-origin checks on mock mutations", async () => {
    const response = await createCharacter(
      mutationRequest(
        "/api/characters",
        { name: "거부될 캐릭터", category: "creator" },
        "http://different.example",
      ),
    );

    expect(response.status).toBe(403);
    expect(await response.json()).toMatchObject({ code: "INVALID_ORIGIN" });
  });
});
