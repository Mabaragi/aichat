import { handleCredentialAuth } from "@/server/auth-handler";

export async function POST(request: Request) {
  return handleCredentialAuth(request, "/api/auth/signup");
}
