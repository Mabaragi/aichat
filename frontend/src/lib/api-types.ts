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

export type PlatformStat = {
  label: string;
  value: string;
};

export type PlatformCategory = {
  id: string;
  label: string;
  description: string;
};

export type PlatformCharacterSpotlight = {
  id: string;
  name: string;
  role: string;
  description: string;
  model: "MOCK" | "FAST" | "BALANCED" | "QUALITY";
  stats: PlatformStat[];
};

export type PlatformDebateCard = {
  id: string;
  title: string;
  description: string;
  categoryId: string;
  categoryLabel: string;
  status: "HOT" | "NEW" | "LIVE" | "READY";
  format: "PROS_AND_CONS" | "FREE_DISCUSSION";
  participants: string[];
  models: Array<"MOCK" | "FAST" | "BALANCED" | "QUALITY">;
  stats: PlatformStat[];
  accent: string;
};
