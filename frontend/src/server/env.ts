export type ServerConfig = {
  backendBaseUrl: string;
  authCookieSecure: boolean;
};

export function getServerConfig(
  environment: NodeJS.ProcessEnv = process.env,
): ServerConfig {
  const backendBaseUrl = environment.BACKEND_BASE_URL?.trim();
  const secureValue = environment.AUTH_COOKIE_SECURE?.trim().toLowerCase();

  if (!backendBaseUrl) {
    throw new Error("BACKEND_BASE_URL is required");
  }

  if (secureValue !== "true" && secureValue !== "false") {
    throw new Error("AUTH_COOKIE_SECURE must be either true or false");
  }

  return {
    backendBaseUrl: backendBaseUrl.replace(/\/+$/, ""),
    authCookieSecure: secureValue === "true",
  };
}
