import { ACCESS_COOKIE } from "@/server/auth-cookies";
import {
  backendFetch,
  bearerHeaders,
  forwardResponse,
  isNextResponse,
} from "@/server/backend";
import { validateSameOrigin } from "@/server/origin";
import { cookies } from "next/headers";

export async function proxyAuthenticatedMutation(
  request: Request,
  backendPath: string,
) {
  const originError = validateSameOrigin(request);
  if (originError) {
    return originError;
  }

  const accessToken = (await cookies()).get(ACCESS_COOKIE)?.value;
  const upstream = await backendFetch(backendPath, {
    method: request.method,
    headers: bearerHeaders(accessToken, {
      "content-type": request.headers.get("content-type") ?? "application/json",
    }),
    body: await request.text(),
  });

  return isNextResponse(upstream) ? upstream : forwardResponse(upstream);
}

export async function proxyAuthenticatedGet(backendPath: string) {
  const accessToken = (await cookies()).get(ACCESS_COOKIE)?.value;
  const upstream = await backendFetch(backendPath, {
    method: "GET",
    headers: bearerHeaders(accessToken),
  });

  return isNextResponse(upstream) ? upstream : forwardResponse(upstream);
}
