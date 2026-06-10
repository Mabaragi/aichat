import type { components } from "@/api/generated/backend-schema";
import { setAuthCookies } from "@/server/auth-cookies";
import {
  backendFetch,
  forwardResponse,
  isNextResponse,
} from "@/server/backend";
import { getServerConfig } from "@/server/env";
import { validateSameOrigin } from "@/server/origin";
import { NextResponse } from "next/server";

type AuthTokensResponse = components["schemas"]["AuthTokensResponse"];

export async function handleCredentialAuth(
  request: Request,
  backendPath: "/api/auth/signup" | "/api/auth/login",
) {
  const originError = validateSameOrigin(request);
  if (originError) {
    return originError;
  }

  const payload = await request.text();
  const upstream = await backendFetch(backendPath, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: payload,
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
    return NextResponse.json(
      {
        code: "INVALID_UPSTREAM_RESPONSE",
        message: "백엔드 인증 응답 형식이 올바르지 않습니다.",
      },
      { status: 502 },
    );
  }

  const response = NextResponse.json(
    { user: auth.user },
    { status: upstream.status },
  );
  setAuthCookies(response, {
    accessToken: auth.accessToken,
    refreshToken: auth.refreshToken,
    expiresIn: auth.expiresIn,
  }, getServerConfig().authCookieSecure);
  return response;
}
