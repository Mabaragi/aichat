import { NextResponse } from "next/server";

export type BffError = {
  code: string;
  message: string;
};

export function errorResponse(
  status: number,
  code: string,
  message: string,
): NextResponse<BffError> {
  return NextResponse.json({ code, message }, { status });
}

export function upstreamUnavailable(): NextResponse<BffError> {
  return errorResponse(
    502,
    "UPSTREAM_UNAVAILABLE",
    "백엔드 서비스에 연결할 수 없습니다.",
  );
}
