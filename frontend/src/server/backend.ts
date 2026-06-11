import { getServerConfig } from "@/server/env";
import { upstreamUnavailable } from "@/server/errors";
import { mockBackendFetch } from "@/server/mock-api";
import { NextResponse } from "next/server";

export async function backendFetch(
  path: string,
  init: RequestInit = {},
): Promise<Response | NextResponse> {
  const { apiMode, backendBaseUrl } = getServerConfig();
  if (apiMode === "mock") {
    return mockBackendFetch(path, init);
  }

  try {
    return await fetch(`${backendBaseUrl}${path}`, {
      ...init,
      cache: "no-store",
      headers: {
        accept: "application/json",
        ...init.headers,
      },
    });
  } catch {
    return upstreamUnavailable();
  }
}

export function bearerHeaders(
  accessToken: string | undefined,
  headers: HeadersInit = {},
): HeadersInit {
  return accessToken
    ? { ...headers, authorization: `Bearer ${accessToken}` }
    : headers;
}

export async function forwardResponse(upstream: Response): Promise<NextResponse> {
  const body = await upstream.text();
  const contentType = upstream.headers.get("content-type");
  const responseHeaders = contentType ? { "content-type": contentType } : undefined;

  return new NextResponse(body || null, {
    status: upstream.status,
    headers: responseHeaders,
  });
}

export function isNextResponse(
  response: Response | NextResponse,
): response is NextResponse {
  return response instanceof NextResponse;
}
