"use client";

import type { Character, CharacterCreate, User } from "@/lib/api-types";
import { ApiError, bffFetch, readJson } from "@/lib/bff-fetch";
import { parseOptionalJsonObject } from "@/lib/json-object";
import { FormEvent, useState, useTransition } from "react";

type CharacterPanelProps = {
  user: User;
  characters: Character[];
  onCreated: (character: Character) => void;
  onLogout: () => void;
};

export function CharacterPanel({
  user,
  characters,
  onCreated,
  onLogout,
}: CharacterPanelProps) {
  const [expanded, setExpanded] = useState(false);
  const [error, setError] = useState<string>();
  const [isPending, startTransition] = useTransition();

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);

    let payload: CharacterCreate;
    try {
      payload = {
        name: String(data.get("name") ?? "").trim(),
        description: String(data.get("description") ?? "").trim() || undefined,
        personality: parseOptionalJsonObject(
          String(data.get("personality") ?? ""),
          "성격",
        ),
        speechStyle: parseOptionalJsonObject(
          String(data.get("speechStyle") ?? ""),
          "말투",
        ),
        visibility: String(data.get("visibility") ?? "PRIVATE") as
          | "PUBLIC"
          | "PRIVATE",
      };
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "JSON을 확인해 주세요.");
      return;
    }

    if (!payload.name) {
      setError("캐릭터 이름을 입력해 주세요.");
      return;
    }

    startTransition(async () => {
      setError(undefined);
      try {
        const response = await bffFetch("/api/characters", {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify(payload),
        });
        const character = await readJson<Character>(response);
        form.reset();
        setExpanded(false);
        onCreated(character);
      } catch (caught) {
        setError(
          caught instanceof ApiError
            ? caught.message
            : "캐릭터를 만들지 못했습니다.",
        );
      }
    });
  }

  return (
    <aside className="context-panel">
      <header className="profile-header">
        <div>
          <p className="eyebrow">MY WORKSPACE</p>
          <h2>{user.nickname ?? "토론가"}</h2>
          <p>{user.email}</p>
        </div>
        <button className="text-button" type="button" onClick={onLogout}>
          로그아웃
        </button>
      </header>

      <div className="section-heading">
        <div>
          <p className="eyebrow">CHARACTERS</p>
          <h3>내 캐릭터</h3>
        </div>
        <span>{characters.length.toString().padStart(2, "0")}</span>
      </div>

      <div className="character-list" aria-live="polite">
        {characters.length === 0 ? (
          <p className="empty-copy">
            아직 캐릭터가 없습니다. 첫 번째 관점을 만들어 주세요.
          </p>
        ) : (
          characters.map((character, index) => (
            <article className="character-row" key={character.id ?? index}>
              <span>{String(index + 1).padStart(2, "0")}</span>
              <div>
                <h4>{character.name ?? "이름 없음"}</h4>
                <p>{character.description || "설명 미설정"}</p>
              </div>
              <small>{character.visibility === "PUBLIC" ? "공개" : "비공개"}</small>
            </article>
          ))
        )}
      </div>

      <button
        className="outline-button"
        type="button"
        aria-expanded={expanded}
        onClick={() => {
          setError(undefined);
          setExpanded((current) => !current);
        }}
      >
        {expanded ? "작성 닫기" : "새 캐릭터 만들기"}
      </button>

      {expanded ? (
        <form className="character-form field-reveal" onSubmit={handleSubmit}>
          <label>
            이름
            <input name="name" maxLength={50} required />
          </label>
          <label>
            설명
            <textarea name="description" maxLength={1000} rows={3} />
          </label>
          <label>
            성격 JSON
            <textarea
              name="personality"
              rows={3}
              defaultValue={'{"rationality": 80}'}
              spellCheck={false}
            />
          </label>
          <label>
            말투 JSON
            <textarea
              name="speechStyle"
              rows={3}
              defaultValue={'{"tone": "calm"}'}
              spellCheck={false}
            />
          </label>
          <label>
            공개 범위
            <select name="visibility" defaultValue="PRIVATE">
              <option value="PRIVATE">비공개</option>
              <option value="PUBLIC">공개</option>
            </select>
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit" disabled={isPending}>
            {isPending ? "저장 중" : "캐릭터 저장"}
          </button>
        </form>
      ) : null}
    </aside>
  );
}
