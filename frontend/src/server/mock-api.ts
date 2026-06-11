import {
  type CreateCharacterRequest,
  type CreateDebateSessionRequest,
  type LoginRequest,
  type RefreshTokenRequest,
  type SignupRequest,
} from "@/server/mock-data/fixtures";
import { getMockStore, MockStoreError } from "@/server/mock-data/store";

const MOCK_ORIGIN = "http://mock.local";

export async function mockBackendFetch(
  path: string,
  init: RequestInit = {},
): Promise<Response> {
  const url = new URL(path, MOCK_ORIGIN);
  const method = (init.method ?? "GET").toUpperCase();
  const headers = new Headers(init.headers);
  const accessToken = readBearer(headers);
  const store = getMockStore();

  try {
    if (method === "GET" && url.pathname === "/api/public/categories") {
      return jsonResponse(store.listCategories(url.searchParams.get("scope")));
    }

    if (method === "GET" && url.pathname === "/api/public/debate-sessions") {
      return jsonResponse(store.listPublicDebates(url.searchParams));
    }

    const publicDebateMatch = url.pathname.match(
      /^\/api\/public\/debate-sessions\/(\d+)$/,
    );
    if (method === "GET" && publicDebateMatch) {
      return jsonResponse(store.getPublicDebate(Number(publicDebateMatch[1])));
    }

    const publicDebateTurnsMatch = url.pathname.match(
      /^\/api\/public\/debate-sessions\/(\d+)\/turns$/,
    );
    if (method === "GET" && publicDebateTurnsMatch) {
      return jsonResponse(
        store.listPublicDebateTurns(Number(publicDebateTurnsMatch[1])),
      );
    }

    if (method === "GET" && url.pathname === "/api/public/characters") {
      return jsonResponse(store.listPublicCharacters(url.searchParams));
    }

    const publicCharacterMatch = url.pathname.match(
      /^\/api\/public\/characters\/(\d+)$/,
    );
    if (method === "GET" && publicCharacterMatch) {
      return jsonResponse(store.getPublicCharacter(Number(publicCharacterMatch[1])));
    }

    if (method === "POST" && url.pathname === "/api/auth/login") {
      return jsonResponse(store.login(await readJsonBody<LoginRequest>(init)));
    }

    if (method === "POST" && url.pathname === "/api/auth/signup") {
      return jsonResponse(store.signup(await readJsonBody<SignupRequest>(init)), 201);
    }

    if (method === "POST" && url.pathname === "/api/auth/refresh") {
      return jsonResponse(store.refresh(await readJsonBody<RefreshTokenRequest>(init)));
    }

    if (method === "POST" && url.pathname === "/api/auth/logout") {
      const payload = await readOptionalJsonBody<RefreshTokenRequest>(init);
      store.logout(payload?.refreshToken);
      return new Response(null, { status: 204 });
    }

    if (method === "GET" && url.pathname === "/api/users/me") {
      return jsonResponse(store.getCurrentUser(accessToken));
    }

    if (method === "GET" && url.pathname === "/api/characters") {
      return jsonResponse(
        store.listCharactersForOwner(
          accessToken,
          parseOptionalNumber(url.searchParams.get("ownerId")),
        ),
      );
    }

    if (method === "POST" && url.pathname === "/api/characters") {
      return jsonResponse(
        store.createCharacter(
          accessToken,
          await readJsonBody<CreateCharacterRequest>(init),
        ),
        201,
      );
    }

    if (method === "POST" && url.pathname === "/api/debate-sessions") {
      return jsonResponse(
        store.createDebateSession(
          accessToken,
          await readJsonBody<CreateDebateSessionRequest>(init),
        ),
        201,
      );
    }

    const debateStartMatch = url.pathname.match(
      /^\/api\/debate-sessions\/(\d+)\/start$/,
    );
    if (method === "POST" && debateStartMatch) {
      return jsonResponse(store.startSession(accessToken, Number(debateStartMatch[1])));
    }

    const debateCompleteMatch = url.pathname.match(
      /^\/api\/debate-sessions\/(\d+)\/complete$/,
    );
    if (method === "POST" && debateCompleteMatch) {
      return jsonResponse(
        store.completeSession(accessToken, Number(debateCompleteMatch[1])),
      );
    }

    const debateGenerateMatch = url.pathname.match(
      /^\/api\/debate-sessions\/(\d+)\/turns\/generate$/,
    );
    if (method === "POST" && debateGenerateMatch) {
      return jsonResponse(
        store.generateTurn(accessToken, Number(debateGenerateMatch[1])),
      );
    }

    const debateTurnsMatch = url.pathname.match(
      /^\/api\/debate-sessions\/(\d+)\/turns$/,
    );
    if (method === "GET" && debateTurnsMatch) {
      return jsonResponse(
        store.listAuthenticatedTurns(accessToken, Number(debateTurnsMatch[1])),
      );
    }

    return jsonResponse(
      {
        code: "DEBATE_SESSION_NOT_FOUND",
        message: "Mock API route를 찾을 수 없습니다.",
        timestamp: new Date().toISOString(),
      },
      404,
    );
  } catch (caught) {
    if (caught instanceof MockStoreError) {
      return jsonResponse(
        {
          code: caught.code,
          message: caught.message,
          timestamp: new Date().toISOString(),
        },
        caught.status,
      );
    }

    throw caught;
  }
}

function jsonResponse(payload: unknown, status = 200): Response {
  return Response.json(payload, {
    status,
    headers: { "content-type": "application/json" },
  });
}

async function readJsonBody<T>(init: RequestInit): Promise<T> {
  const payload = await readOptionalJsonBody<T>(init);
  if (!payload) {
    return {} as T;
  }

  return payload;
}

async function readOptionalJsonBody<T>(init: RequestInit): Promise<T | undefined> {
  const body = init.body;
  if (!body) {
    return undefined;
  }

  const text =
    typeof body === "string"
      ? body
      : body instanceof Blob
        ? await body.text()
        : body instanceof URLSearchParams
          ? body.toString()
          : body instanceof ReadableStream
            ? await new Response(body).text()
            : "";

  if (!text) {
    return undefined;
  }

  return JSON.parse(text) as T;
}

function readBearer(headers: Headers): string | undefined {
  const value = headers.get("authorization");
  const prefix = "Bearer ";
  return value?.startsWith(prefix) ? value.slice(prefix.length) : undefined;
}

function parseOptionalNumber(value: string | null) {
  if (!value) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}
