import { proxyPublicGet } from "@/server/public-proxy";

export async function GET(request: Request) {
  const { search } = new URL(request.url);
  return proxyPublicGet(`/api/public/characters${search}`);
}
