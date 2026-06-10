import type { NextResponse } from "next/server";

export const ACCESS_COOKIE = "aichat_access";
export const REFRESH_COOKIE = "aichat_refresh";

type TokenPair = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
};

export function setAuthCookies(
  response: NextResponse,
  tokens: TokenPair,
  secure: boolean,
) {
  const common = {
    httpOnly: true,
    sameSite: "lax" as const,
    secure,
    path: "/",
  };

  response.cookies.set(ACCESS_COOKIE, tokens.accessToken, {
    ...common,
    maxAge: tokens.expiresIn,
  });
  response.cookies.set(REFRESH_COOKIE, tokens.refreshToken, common);
}

export function clearAuthCookies(response: NextResponse, secure: boolean) {
  const expired = {
    httpOnly: true,
    sameSite: "lax" as const,
    secure,
    path: "/",
    maxAge: 0,
  };

  response.cookies.set(ACCESS_COOKIE, "", expired);
  response.cookies.set(REFRESH_COOKIE, "", expired);
}
