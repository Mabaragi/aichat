import { errorResponse } from "@/server/errors";

export function validateSameOrigin(request: Request) {
  const origin = request.headers.get("origin");
  const requestUrl = new URL(request.url);

  if (!origin) {
    return errorResponse(
      403,
      "INVALID_ORIGIN",
      "같은 출처에서 보낸 요청만 허용됩니다.",
    );
  }

  const originUrl = new URL(origin);
  const forwardedHost = request.headers
    .get("x-forwarded-host")
    ?.split(",")[0]
    ?.trim();
  const forwardedProtocol = request.headers
    .get("x-forwarded-proto")
    ?.split(",")[0]
    ?.trim();
  const expectedHost =
    forwardedHost ?? request.headers.get("host") ?? requestUrl.host;
  const expectedProtocol = forwardedProtocol
    ? `${forwardedProtocol}:`
    : requestUrl.protocol;

  if (
    originUrl.host !== expectedHost ||
    originUrl.protocol !== expectedProtocol
  ) {
    return errorResponse(
      403,
      "INVALID_ORIGIN",
      "같은 출처에서 보낸 요청만 허용됩니다.",
    );
  }

  return null;
}
