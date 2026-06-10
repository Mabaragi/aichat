import type { components } from "@/api/generated/backend-schema";
import { ACCESS_COOKIE } from "@/server/auth-cookies";
import {
  backendFetch,
  bearerHeaders,
  forwardResponse,
  isNextResponse,
} from "@/server/backend";
import { errorResponse } from "@/server/errors";
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

type UserResponse = components["schemas"]["UserResponse"];
type CharacterResponse = components["schemas"]["CharacterResponse"];

export async function GET() {
  const accessToken = (await cookies()).get(ACCESS_COOKIE)?.value;
  if (!accessToken) {
    return errorResponse(401, "UNAUTHORIZED", "로그인이 필요합니다.");
  }

  const userResponse = await backendFetch("/api/users/me", {
    headers: bearerHeaders(accessToken),
  });
  if (isNextResponse(userResponse)) {
    return userResponse;
  }
  if (!userResponse.ok) {
    return forwardResponse(userResponse);
  }

  const user = (await userResponse.json()) as UserResponse;
  const charactersResponse = await backendFetch(
    `/api/characters?ownerId=${encodeURIComponent(String(user.id))}`,
    { headers: bearerHeaders(accessToken) },
  );
  if (isNextResponse(charactersResponse)) {
    return charactersResponse;
  }
  if (!charactersResponse.ok) {
    return forwardResponse(charactersResponse);
  }

  const characters = (await charactersResponse.json()) as CharacterResponse[];
  return NextResponse.json({ user, characters });
}
