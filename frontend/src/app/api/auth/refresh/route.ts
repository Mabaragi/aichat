import type { components } from "@/api/generated/backend-schema";
import {
  REFRESH_COOKIE,
  setAuthCookies,
} from "@/server/auth-cookies";
import {
  backendFetch,
  forwardResponse,
  isNextResponse,
} from "@/server/backend";
import { getServerConfig } from "@/server/env";
import { errorResponse } from "@/server/errors";
import { validateSameOrigin } from "@/server/origin";
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

type AuthTokensResponse = components["schemas"]["AuthTokensResponse"];

export async function POST(request: Request) {
  const originError = validateSameOrigin(request);
  if (originError) {
    return originError;
  }

  const refreshToken = (await cookies()).get(REFRESH_COOKIE)?.value;
  if (!refreshToken) {
    return errorResponse(401, "INVALID_TOKEN", "Refresh token이 없습니다.");
  }

  const upstream = await backendFetch("/api/auth/refresh", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (isNextResponse(upstream)) {
    return upstream;
  }
  if (!upstream.ok) {
    return forwardResponse(upstream);
  }

  const auth = (await upstream.json()) as AuthTokensResponse;
  if (
    !auth.user ||
    !auth.accessToken ||
    !auth.refreshToken ||
    typeof auth.expiresIn !== "number"
  ) {
    return errorResponse(
      502,
      "INVALID_UPSTREAM_RESPONSE",
      "백엔드 인증 응답 형식이 올바르지 않습니다.",
    );
  }

  const response = NextResponse.json({ user: auth.user });
  setAuthCookies(response, {
    accessToken: auth.accessToken,
    refreshToken: auth.refreshToken,
    expiresIn: auth.expiresIn,
  }, getServerConfig().authCookieSecure);
  return response;
}
