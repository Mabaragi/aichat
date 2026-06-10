import { validateSameOrigin } from "@/server/origin";
import { describe, expect, it } from "vitest";

describe("validateSameOrigin", () => {
  it("uses the external Host header when the container URL is internal", () => {
    const request = new Request("http://0.0.0.0:3000/api/characters", {
      method: "POST",
      headers: {
        host: "127.0.0.1:3000",
        origin: "http://127.0.0.1:3000",
      },
    });

    expect(validateSameOrigin(request)).toBeNull();
  });

  it("supports forwarded host and protocol from a reverse proxy", () => {
    const request = new Request("http://aichat-frontend:3000/api/characters", {
      method: "POST",
      headers: {
        host: "aichat-frontend:3000",
        origin: "https://debate.example.com",
        "x-forwarded-host": "debate.example.com",
        "x-forwarded-proto": "https",
      },
    });

    expect(validateSameOrigin(request)).toBeNull();
  });
});
