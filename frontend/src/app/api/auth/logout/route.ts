import { REFRESH_COOKIE, clearAuthCookies } from "@/server/auth-cookies";
import {
  backendFetch,
  forwardResponse,
  isNextResponse,
} from "@/server/backend";
import { getServerConfig } from "@/server/env";
import { validateSameOrigin } from "@/server/origin";
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const originError = validateSameOrigin(request);
  if (originError) {
    return originError;
  }

  const refreshToken = (await cookies()).get(REFRESH_COOKIE)?.value;
  const upstream = await backendFetch("/api/auth/logout", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: refreshToken ? JSON.stringify({ refreshToken }) : undefined,
  });

  let response: NextResponse;
  if (isNextResponse(upstream)) {
    response = upstream;
  } else if (upstream.ok) {
    response = new NextResponse(null, { status: 204 });
  } else {
    response = await forwardResponse(upstream);
  }

  clearAuthCookies(response, getServerConfig().authCookieSecure);
  return response;
}
