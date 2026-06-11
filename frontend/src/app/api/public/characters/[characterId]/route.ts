import { proxyPublicGet } from "@/server/public-proxy";

type RouteContext = {
  params: Promise<{ characterId: string }>;
};

export async function GET(_request: Request, context: RouteContext) {
  const { characterId } = await context.params;
  return proxyPublicGet(
    `/api/public/characters/${encodeURIComponent(characterId)}`,
  );
}
