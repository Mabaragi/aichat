import { proxyAuthenticatedGet } from "@/server/authenticated-proxy";

type RouteContext = {
  params: Promise<{ sessionId: string }>;
};

export async function GET(_request: Request, context: RouteContext) {
  const { sessionId } = await context.params;
  return proxyAuthenticatedGet(
    `/api/debate-sessions/${encodeURIComponent(sessionId)}/turns`,
  );
}
