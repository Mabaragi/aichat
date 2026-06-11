export type ServerConfig = {
  backendBaseUrl: string;
  authCookieSecure: boolean;
  apiMode: "backend" | "mock";
};

export function getServerConfig(
  environment: NodeJS.ProcessEnv = process.env,
): ServerConfig {
  const backendBaseUrl = environment.BACKEND_BASE_URL?.trim();
  const secureValue = environment.AUTH_COOKIE_SECURE?.trim().toLowerCase();
  const apiModeValue = environment.FRONTEND_API_MODE?.trim().toLowerCase() ?? "backend";

  if (apiModeValue !== "backend" && apiModeValue !== "mock") {
    throw new Error("FRONTEND_API_MODE must be either backend or mock");
  }

  if (environment.NODE_ENV === "production" && apiModeValue === "mock") {
    throw new Error("FRONTEND_API_MODE=mock is not allowed in production");
  }

  if (apiModeValue === "backend" && !backendBaseUrl) {
    throw new Error("BACKEND_BASE_URL is required");
  }

  if (secureValue !== "true" && secureValue !== "false") {
    throw new Error("AUTH_COOKIE_SECURE must be either true or false");
  }

  return {
    backendBaseUrl: (backendBaseUrl ?? "http://mock-backend.invalid").replace(
      /\/+$/,
      "",
    ),
    authCookieSecure: secureValue === "true",
    apiMode: apiModeValue,
  };
}
