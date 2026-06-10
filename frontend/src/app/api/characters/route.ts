import { proxyAuthenticatedMutation } from "@/server/authenticated-proxy";

export async function POST(request: Request) {
  return proxyAuthenticatedMutation(request, "/api/characters");
}
