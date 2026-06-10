import type { components } from "@/api/generated/backend-schema";

export type User = components["schemas"]["UserResponse"];
export type Character = components["schemas"]["CharacterResponse"];
export type CharacterCreate =
  components["schemas"]["CreateCharacterRequest"];
export type DebateCreate =
  components["schemas"]["CreateDebateSessionRequest"];
export type DebateSession = components["schemas"]["DebateSessionResponse"];
export type ErrorPayload = Omit<
  components["schemas"]["ErrorResponse"],
  "code" | "message"
> & {
  code?: string;
  message?: string;
};

export type WorkspaceData = {
  user: User;
  characters: Character[];
};
