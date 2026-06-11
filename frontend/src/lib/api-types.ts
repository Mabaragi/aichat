import type { components } from "@/api/generated/backend-schema";

export type User = components["schemas"]["UserResponse"];
export type Category = components["schemas"]["CategorySummaryResponse"];
export type Character = components["schemas"]["CharacterResponse"];
export type CharacterCreate =
  components["schemas"]["CreateCharacterRequest"];
export type DebateCreate =
  components["schemas"]["CreateDebateSessionRequest"];
export type DebateSession = components["schemas"]["DebateSessionResponse"];
export type DebateTurn = components["schemas"]["DebateTurnResponse"];
export type GeneratedDebateTurn = components["schemas"]["GenerateTurnResponse"];
export type DebateLifecycle = {
  id?: number;
  status?: DebateSession["status"];
  startedAt?: string;
  endedAt?: string;
};
export type PublicCharacterPage =
  components["schemas"]["PublicCharacterPageResponse"];
export type PublicDebatePage =
  components["schemas"]["PublicDebateSessionPageResponse"];
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
