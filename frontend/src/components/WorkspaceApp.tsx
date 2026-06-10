"use client";

import { AuthPanel } from "@/components/AuthPanel";
import { CharacterPanel } from "@/components/CharacterPanel";
import { DebateComposer } from "@/components/DebateComposer";
import type { Character, User, WorkspaceData } from "@/lib/api-types";
import { bffFetch, workspaceFetcher } from "@/lib/bff-fetch";
import useSWR from "swr";

export function WorkspaceApp() {
  const { data, error, isLoading, mutate } = useSWR<WorkspaceData>(
    "/api/workspace",
    workspaceFetcher,
    {
      shouldRetryOnError: false,
      revalidateOnFocus: false,
    },
  );

  function handleAuthenticated(user: User) {
    void mutate({ user, characters: [] }, { revalidate: true });
  }

  function handleCharacterCreated(character: Character) {
    void mutate(
      (current) =>
        current
          ? { ...current, characters: [...current.characters, character] }
          : current,
      { revalidate: false },
    );
  }

  async function handleLogout() {
    await bffFetch("/api/auth/logout", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: "{}",
    });
    await mutate(undefined, { revalidate: false });
  }

  if (isLoading) {
    return (
      <main className="loading-shell">
        <p className="eyebrow">AI DEBATE STUDIO</p>
        <div className="loading-line" />
        <p>작업 공간을 준비하고 있습니다.</p>
      </main>
    );
  }

  if (!data || error) {
    return <AuthPanel onAuthenticated={handleAuthenticated} />;
  }

  return (
    <main className="workspace-shell">
      <CharacterPanel
        user={data.user}
        characters={data.characters}
        onCreated={handleCharacterCreated}
        onLogout={handleLogout}
      />
      <DebateComposer characters={data.characters} />
    </main>
  );
}
