"use client";

import type {
  Character,
  DebateCreate,
  DebateSession,
} from "@/lib/api-types";
import { ApiError, bffFetch, readJson } from "@/lib/bff-fetch";
import { FormEvent, useState, useTransition } from "react";

type DebateComposerProps = {
  characters: Character[];
};

const MODELS = ["FAST", "BALANCED", "QUALITY", "MOCK"] as const;

export function DebateComposer({ characters }: DebateComposerProps) {
  const firstCharacterId = characters[0]?.id;
  const [leftId, setLeftId] = useState<number | undefined>(firstCharacterId);
  const [rightId, setRightId] = useState<number | undefined>(firstCharacterId);
  const [result, setResult] = useState<DebateSession>();
  const [error, setError] = useState<string>();
  const [isPending, startTransition] = useTransition();

  const effectiveLeftId = leftId ?? firstCharacterId;
  const effectiveRightId = rightId ?? firstCharacterId;
  const selectedLeft = characters.find(
    (character) => character.id === effectiveLeftId,
  );
  const selectedRight = characters.find(
    (character) => character.id === effectiveRightId,
  );

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);

    if (effectiveLeftId === undefined || effectiveRightId === undefined) {
      setError("두 참가자를 모두 선택해 주세요.");
      return;
    }

    const payload: DebateCreate = {
      topic: {
        title: String(data.get("title") ?? "").trim(),
        description: String(data.get("description") ?? "").trim(),
        category: String(data.get("category") ?? "").trim() || undefined,
      },
      format: String(data.get("format")) as DebateCreate["format"],
      maxRounds: Number(data.get("maxRounds")),
      maxTurnLength: Number(data.get("maxTurnLength")),
      participants: [
        {
          characterId: effectiveLeftId,
          model: String(data.get("leftModel")) as
            DebateCreate["participants"][number]["model"],
        },
        {
          characterId: effectiveRightId,
          model: String(data.get("rightModel")) as
            DebateCreate["participants"][number]["model"],
        },
      ],
    };

    if (!payload.topic.title || !payload.topic.description) {
      setError("주제 제목과 설명을 입력해 주세요.");
      return;
    }

    startTransition(async () => {
      setError(undefined);
      try {
        const response = await bffFetch("/api/debate-sessions", {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify(payload),
        });
        setResult(await readJson<DebateSession>(response));
      } catch (caught) {
        setError(
          caught instanceof ApiError
            ? caught.message
            : "토론 세션을 만들지 못했습니다.",
        );
      }
    });
  }

  return (
    <section className="debate-stage">
      <header className="stage-header">
        <div>
          <p className="eyebrow">NEW DEBATE</p>
          <h1>토론 설계</h1>
        </div>
        <p>
          두 캐릭터와 모델을 순서대로 배치합니다.
          <br />
          같은 캐릭터를 양쪽에 선택할 수 있습니다.
        </p>
      </header>

      {characters.length === 0 ? (
        <div className="stage-empty">
          <span>01</span>
          <h2>먼저 캐릭터를 만들어 주세요.</h2>
          <p>왼쪽 패널에서 토론에 참여할 관점을 정의할 수 있습니다.</p>
        </div>
      ) : (
        <form className="debate-form" onSubmit={handleSubmit}>
          <fieldset className="topic-fields">
            <legend>주제</legend>
            <label className="wide-field">
              제목
              <input
                name="title"
                maxLength={200}
                placeholder="부먹 vs 찍먹"
                required
              />
            </label>
            <label className="wide-field">
              설명
              <textarea
                name="description"
                rows={3}
                placeholder="어느 방식이 더 나은가?"
                required
              />
            </label>
            <label>
              카테고리
              <input name="category" placeholder="FOOD" />
            </label>
            <label>
              형식
              <select name="format" defaultValue="PROS_AND_CONS">
                <option value="PROS_AND_CONS">찬반 토론</option>
                <option value="FREE_DISCUSSION">자유 토론</option>
              </select>
            </label>
            <label>
              라운드
              <input
                name="maxRounds"
                type="number"
                min={1}
                max={10}
                defaultValue={5}
              />
            </label>
            <label>
              턴 길이
              <input
                name="maxTurnLength"
                type="number"
                min={100}
                max={2000}
                defaultValue={600}
              />
            </label>
          </fieldset>

          <fieldset className="participant-fields">
            <legend>참가자 순서</legend>
            <ParticipantSelect
              index={1}
              characterId={effectiveLeftId}
              character={selectedLeft}
              characters={characters}
              selectName="leftCharacter"
              modelName="leftModel"
              onCharacterChange={setLeftId}
            />
            <div className="versus" aria-hidden="true">
              VS
            </div>
            <ParticipantSelect
              index={2}
              characterId={effectiveRightId}
              character={selectedRight}
              characters={characters}
              selectName="rightCharacter"
              modelName="rightModel"
              onCharacterChange={setRightId}
            />
          </fieldset>

          {error ? <p className="form-error stage-error">{error}</p> : null}
          <button className="create-debate-button" type="submit" disabled={isPending}>
            <span>{isPending ? "생성 중" : "토론 세션 생성"}</span>
            <span aria-hidden="true">→</span>
          </button>
        </form>
      )}

      {result ? <DebateResult result={result} /> : null}
    </section>
  );
}

type ParticipantSelectProps = {
  index: number;
  characterId: number | undefined;
  character: Character | undefined;
  characters: Character[];
  selectName: string;
  modelName: string;
  onCharacterChange: (id: number) => void;
};

function ParticipantSelect({
  index,
  characterId,
  character,
  characters,
  selectName,
  modelName,
  onCharacterChange,
}: ParticipantSelectProps) {
  return (
    <div className="participant-select">
      <span className="participant-index">{String(index).padStart(2, "0")}</span>
      <div className="participant-name">
        <strong>{character?.name ?? "참가자 선택"}</strong>
        <small>{character?.description || "설명 미설정"}</small>
      </div>
      <label>
        캐릭터
        <select
          name={selectName}
          value={characterId ?? ""}
          onChange={(event) => onCharacterChange(Number(event.target.value))}
        >
          {characters.map((option, optionIndex) => (
            <option key={option.id ?? optionIndex} value={option.id}>
              {option.name}
            </option>
          ))}
        </select>
      </label>
      <label>
        모델
        <select name={modelName} defaultValue={index === 1 ? "FAST" : "QUALITY"}>
          {MODELS.map((model) => (
            <option key={model} value={model}>
              {model}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}

function DebateResult({ result }: { result: DebateSession }) {
  return (
    <section className="debate-result" aria-live="polite">
      <header>
        <div>
          <p className="eyebrow">SESSION CREATED</p>
          <h2>{result.topicTitle}</h2>
        </div>
        <span>{result.status ?? "CREATED"}</span>
      </header>
      <p>{result.topicDescription}</p>
      <div className="result-participants">
        {(result.participants ?? []).map((participant, index) => (
          <article key={participant.id ?? index}>
            <span>{String((participant.position ?? index) + 1).padStart(2, "0")}</span>
            <div>
              <strong>{participant.name}</strong>
              <small>
                {participant.model} · character #{participant.sourceCharacterId}
              </small>
            </div>
            <dl>
              <dt>성격</dt>
              <dd>{formatSnapshot(participant.personality)}</dd>
              <dt>말투</dt>
              <dd>{formatSnapshot(participant.speechStyle)}</dd>
            </dl>
          </article>
        ))}
      </div>
    </section>
  );
}

function formatSnapshot(value: Record<string, unknown> | undefined) {
  return value ? JSON.stringify(value) : "미설정";
}
