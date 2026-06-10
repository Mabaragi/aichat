import { bffFetch, resetRefreshForTest } from "@/lib/bff-fetch";
import { afterEach, describe, expect, it, vi } from "vitest";

describe("bffFetch", () => {
  afterEach(() => {
    resetRefreshForTest();
    vi.unstubAllGlobals();
  });

  it("uses one refresh request for concurrent 401 responses and retries once", async () => {
    let workspaceCalls = 0;
    let refreshCalls = 0;
    const fetchMock = vi.fn(async (input: RequestInfo | URL) => {
      if (String(input) === "/api/auth/refresh") {
        refreshCalls += 1;
        await Promise.resolve();
        return new Response("{}", { status: 200 });
      }

      workspaceCalls += 1;
      return new Response("{}", {
        status: workspaceCalls <= 2 ? 401 : 200,
      });
    });
    vi.stubGlobal("fetch", fetchMock);

    const [first, second] = await Promise.all([
      bffFetch("/api/workspace"),
      bffFetch("/api/workspace"),
    ]);

    expect(first.status).toBe(200);
    expect(second.status).toBe(200);
    expect(refreshCalls).toBe(1);
    expect(workspaceCalls).toBe(4);
  });

  it("returns the original 401 when refresh fails", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(new Response("{}", { status: 401 }))
      .mockResolvedValueOnce(new Response("{}", { status: 401 }));
    vi.stubGlobal("fetch", fetchMock);

    const response = await bffFetch("/api/workspace");

    expect(response.status).toBe(401);
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
