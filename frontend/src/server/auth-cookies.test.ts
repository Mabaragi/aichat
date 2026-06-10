import {
  ACCESS_COOKIE,
  REFRESH_COOKIE,
  clearAuthCookies,
  setAuthCookies,
} from "@/server/auth-cookies";
import { NextResponse } from "next/server";
import { describe, expect, it } from "vitest";

describe("auth cookies", () => {
  it("sets secure cookies when runtime configuration requires HTTPS", () => {
    const response = NextResponse.json({});

    setAuthCookies(
      response,
      {
        accessToken: "access",
        refreshToken: "refresh",
        expiresIn: 60,
      },
      true,
    );

    const cookies = response.headers.getSetCookie().join("; ");
    expect(cookies).toContain(`${ACCESS_COOKIE}=access`);
    expect(cookies).toContain(`${REFRESH_COOKIE}=refresh`);
    expect(cookies).toContain("Secure");
    expect(cookies).toContain("HttpOnly");
  });

  it("expires both cookies during logout", () => {
    const response = NextResponse.json({});

    clearAuthCookies(response, false);

    const cookies = response.headers.getSetCookie().join("; ");
    expect(cookies).toContain(`${ACCESS_COOKIE}=`);
    expect(cookies).toContain(`${REFRESH_COOKIE}=`);
    expect(cookies.match(/Max-Age=0/g)).toHaveLength(2);
  });
});
