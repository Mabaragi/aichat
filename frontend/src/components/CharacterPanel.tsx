"use client";

import type { Category, Character, CharacterCreate, User } from "@/lib/api-types";
import { ApiError, bffFetch, readJson } from "@/lib/bff-fetch";
import { parseOptionalJsonObject } from "@/lib/json-object";
import { FormEvent, useState, useTransition } from "react";

type CharacterPanelProps = {
  user: User;
  characters: Character[];
  categories: Category[];
  onCreated: (character: Character) => void;
  onLogout: () => void;
};

export function CharacterPanel({
  user,
  characters,
  categories,
  onCreated,
  onLogout,
}: CharacterPanelProps) {
  const [expanded, setExpanded] = useState(characters.length === 0);
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
        category: String(data.get("category") ?? "other").trim() || "other",
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

    startTransition(() => {
      void createCharacter(form, payload);
    });
  }

  async function createCharacter(form: HTMLFormElement, payload: CharacterCreate) {
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
  }

  return (
    <section className="creator-panel">
      <header className="creator-header">
        <div>
          <p className="eyebrow">MY CHARACTERS</p>
          <h3>{user.nickname ?? "토론가"}의 캐릭터</h3>
          <p>{user.email}</p>
        </div>
        <button className="ghost-button" type="button" onClick={onLogout}>
          로그아웃
        </button>
      </header>

      <div className="character-stack" aria-live="polite">
        {characters.length === 0 ? (
          <p className="empty-copy">
            아직 캐릭터가 없습니다. 첫 번째 토론자를 만들면 바로 세션을 열 수
            있습니다.
          </p>
        ) : (
          characters.map((character, index) => (
            <article className="character-row" key={character.id ?? index}>
              <span>{String(index + 1).padStart(2, "0")}</span>
              <div>
                <h4>{character.name ?? "이름 없음"}</h4>
                <p>{character.description || "설명 없음"}</p>
              </div>
              <small>
                {character.category?.name ?? "기타"} ·{" "}
                {character.visibility === "PUBLIC" ? "공개" : "비공개"}
              </small>
            </article>
          ))
        )}
      </div>

      <button
        className="secondary-button wide"
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
            <input
              name="name"
              maxLength={50}
              placeholder="합리적 미식가"
              required
            />
          </label>
          <label>
            카테고리
            <select name="category" defaultValue={categories[0]?.slug ?? "other"}>
              {categories.map((category) => (
                <option key={category.id ?? category.slug} value={category.slug}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            설명
            <textarea
              name="description"
              maxLength={1000}
              rows={3}
              placeholder="근거를 차분하게 정리하는 토론자"
            />
          </label>
          <label>
            성격 JSON
            <textarea
              name="personality"
              rows={3}
              defaultValue={'{"rationality": 80, "humor": 30}'}
              spellCheck={false}
            />
          </label>
          <label>
            말투 JSON
            <textarea
              name="speechStyle"
              rows={3}
              defaultValue={'{"tone": "calm", "formality": "medium"}'}
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
          <button className="primary-button wide" type="submit" disabled={isPending}>
            {isPending ? "저장 중" : "캐릭터 저장"}
          </button>
        </form>
      ) : null}
    </section>
  );
}
