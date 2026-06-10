import type { ErrorPayload, WorkspaceData } from "@/lib/api-types";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly payload: ErrorPayload,
  ) {
    super(payload.message ?? "요청을 처리하지 못했습니다.");
  }
}

let refreshInFlight: Promise<boolean> | null = null;

async function refreshSession(): Promise<boolean> {
  refreshInFlight ??= fetch("/api/auth/refresh", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: "{}",
  })
    .then((response) => response.ok)
    .catch(() => false)
    .finally(() => {
      refreshInFlight = null;
    });

  return refreshInFlight;
}

export async function bffFetch(
  input: string,
  init: RequestInit = {},
): Promise<Response> {
  const response = await fetch(input, init);
  if (
    response.status !== 401 ||
    input === "/api/auth/refresh" ||
    input === "/api/auth/login" ||
    input === "/api/auth/signup"
  ) {
    return response;
  }

  const refreshed = await refreshSession();
  return refreshed ? fetch(input, init) : response;
}

export async function readJson<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let payload: ErrorPayload = {
      code: "REQUEST_FAILED",
      message: "요청을 처리하지 못했습니다.",
    };

    try {
      payload = (await response.json()) as ErrorPayload;
    } catch {
      // Keep the normalized fallback when the upstream body is not JSON.
    }
    throw new ApiError(response.status, payload);
  }

  return response.json() as Promise<T>;
}

export async function workspaceFetcher(path: string): Promise<WorkspaceData> {
  return readJson<WorkspaceData>(await bffFetch(path));
}

export function resetRefreshForTest() {
  refreshInFlight = null;
}
