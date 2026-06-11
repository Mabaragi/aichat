import { proxyAuthenticatedMutation } from "@/server/authenticated-proxy";

type RouteContext = {
  params: Promise<{ sessionId: string }>;
};

export async function POST(request: Request, context: RouteContext) {
  const { sessionId } = await context.params;
  return proxyAuthenticatedMutation(
    request,
    `/api/debate-sessions/${encodeURIComponent(sessionId)}/turns/generate`,
  );
}
