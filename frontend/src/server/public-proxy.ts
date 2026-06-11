import { backendFetch, forwardResponse, isNextResponse } from "@/server/backend";

export async function proxyPublicGet(backendPath: string) {
  const upstream = await backendFetch(backendPath, {
    method: "GET",
  });

  return isNextResponse(upstream) ? upstream : forwardResponse(upstream);
}
