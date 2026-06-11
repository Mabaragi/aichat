"use client";

import type {
  Category,
  Character,
  DebateCreate,
  DebateLifecycle,
  DebateSession,
  DebateTurn,
  GeneratedDebateTurn,
} from "@/lib/api-types";
import { ApiError, bffFetch, readJson } from "@/lib/bff-fetch";
import { FormEvent, useEffect, useState, useTransition } from "react";

type DebateComposerProps = {
  characters: Character[];
  categories: Category[];
};

const MODELS = ["FAST", "BALANCED", "QUALITY", "MOCK"] as const;
const SESSION_STORAGE_KEY = "aichat.currentDebate.v1";

type StoredDebateState = {
  session: DebateSession;
  turns: DebateTurn[];
};
type DebateParticipantSnapshot = NonNullable<DebateSession["participants"]>[number];

export function DebateComposer({ characters, categories }: DebateComposerProps) {
  const firstCharacterId = characters[0]?.id;
  const [leftId, setLeftId] = useState<number | undefined>(firstCharacterId);
  const [rightId, setRightId] = useState<number | undefined>(firstCharacterId);
  const [result, setResult] = useState<DebateSession>();
  const [turns, setTurns] = useState<DebateTurn[]>([]);
  const [error, setError] = useState<string>();
  const [stageMessage, setStageMessage] = useState<string>();
  const [isPending, startTransition] = useTransition();

  const effectiveLeftId = leftId ?? firstCharacterId;
  const effectiveRightId = rightId ?? firstCharacterId;
  const selectedLeft = characters.find(
    (character) => character.id === effectiveLeftId,
  );
  const selectedRight = characters.find(
    (character) => character.id === effectiveRightId,
  );

  useEffect(() => {
    let cancelled = false;
    queueMicrotask(() => {
      if (cancelled) {
        return;
      }
      try {
        const raw = sessionStorage.getItem(SESSION_STORAGE_KEY);
        if (!raw) {
          return;
        }
        const parsed = JSON.parse(raw) as StoredDebateState;
        if (parsed.session?.id) {
          setResult(parsed.session);
          setTurns(parsed.turns ?? []);
        }
      } catch {
        sessionStorage.removeItem(SESSION_STORAGE_KEY);
      }
    });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!result) {
      sessionStorage.removeItem(SESSION_STORAGE_KEY);
      return;
    }
    sessionStorage.setItem(
      SESSION_STORAGE_KEY,
      JSON.stringify({ session: result, turns } satisfies StoredDebateState),
    );
  }, [result, turns]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);

    if (effectiveLeftId === undefined || effectiveRightId === undefined) {
      setError("참가 캐릭터를 먼저 만들어 주세요.");
      return;
    }

    const payload: DebateCreate = {
      topic: {
        title: String(data.get("title") ?? "").trim(),
        description: String(data.get("description") ?? "").trim(),
        category: String(data.get("category") ?? "other").trim() || "other",
      },
      format: String(data.get("format")) as DebateCreate["format"],
      visibility: String(data.get("visibility") ?? "PRIVATE") as
        | "PUBLIC"
        | "PRIVATE",
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
      setError("토론 제목과 설명을 입력해 주세요.");
      return;
    }

    startTransition(() => {
      void createDebate(payload);
    });
  }

  async function createDebate(payload: DebateCreate) {
    setError(undefined);
    try {
      const response = await bffFetch("/api/debate-sessions", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(payload),
      });
      const created = await readJson<DebateSession>(response);
      setTurns([]);
      setResult(created);
      setStageMessage("세션이 생성되었습니다. 시작하면 턴을 생성할 수 있습니다.");
    } catch (caught) {
      setError(
        caught instanceof ApiError
          ? caught.message
          : "토론 세션을 만들지 못했습니다.",
      );
    }
  }

  async function startSession() {
    if (!result?.id) {
      return;
    }
    setError(undefined);
    try {
      const response = await bffFetch(`/api/debate-sessions/${result.id}/start`, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: "{}",
      });
      const lifecycle = await readJson<DebateLifecycle>(response);
      setResult((current) =>
        current
          ? {
              ...current,
              status: lifecycle.status,
              startedAt: lifecycle.startedAt,
              endedAt: lifecycle.endedAt,
            }
          : current,
      );
      setStageMessage("토론이 시작되었습니다. 다음 턴을 생성하세요.");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "세션을 시작하지 못했습니다.");
    }
  }

  async function generateTurn() {
    if (!result?.id) {
      return;
    }
    setError(undefined);
    try {
      const response = await bffFetch(
        `/api/debate-sessions/${result.id}/turns/generate`,
        {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: "{}",
        },
      );
      const generated = await readJson<GeneratedDebateTurn>(response);
      const nextTurns = [...turns, generated as DebateTurn];
      setTurns(nextTurns);
      if (
        result.maxRounds !== undefined &&
        result.participants &&
        nextTurns.length >= result.maxRounds * result.participants.length
      ) {
        setResult({ ...result, status: "COMPLETED", endedAt: generated.createdAt });
        setStageMessage("최대 턴 수에 도달해 토론이 완료되었습니다.");
      } else {
        setStageMessage(`${nextTurns.length}번째 턴이 생성되었습니다.`);
      }
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "턴을 생성하지 못했습니다.");
    }
  }

  async function completeSession() {
    if (!result?.id) {
      return;
    }
    setError(undefined);
    try {
      const response = await bffFetch(`/api/debate-sessions/${result.id}/complete`, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: "{}",
      });
      const lifecycle = await readJson<DebateLifecycle>(response);
      setResult((current) =>
        current
          ? {
              ...current,
              status: lifecycle.status,
              endedAt: lifecycle.endedAt,
            }
          : current,
      );
      setStageMessage("토론이 완료되었습니다. PUBLIC 세션이면 공개 목록에 노출됩니다.");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "세션을 완료하지 못했습니다.");
    }
  }

  return (
    <section className="creator-panel debate-creator">
      <header className="creator-header">
        <div>
          <p className="eyebrow">NEW MATCH</p>
          <h3>토론 매치 만들기</h3>
          <p>같은 캐릭터를 양쪽에 배치해 모델만 다르게 비교해도 됩니다.</p>
        </div>
      </header>

      {characters.length === 0 ? (
        <div className="stage-empty">
          <span>01</span>
          <h4>먼저 캐릭터를 만들어 주세요.</h4>
          <p>왼쪽 패널에서 캐릭터를 저장하면 이곳에서 바로 선택할 수 있습니다.</p>
        </div>
      ) : (
        <form className="debate-form" onSubmit={handleSubmit}>
          <fieldset className="topic-fields">
            <legend>토론 주제</legend>
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
                placeholder="어떤 기준으로 더 나은 선택인지 토론합니다."
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
              형식
              <select name="format" defaultValue="PROS_AND_CONS">
                <option value="PROS_AND_CONS">찬반 토론</option>
                <option value="FREE_DISCUSSION">자유 토론</option>
              </select>
            </label>
            <label>
              공개 범위
              <select name="visibility" defaultValue="PRIVATE">
                <option value="PRIVATE">비공개</option>
                <option value="PUBLIC">완료 후 공개</option>
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
            <legend>참가자와 모델</legend>
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
          <button className="primary-button wide" type="submit" disabled={isPending}>
            {isPending ? "세션 생성 중" : "토론 세션 생성"}
          </button>
        </form>
      )}

      {result ? (
        <DebateResult
          result={result}
          turns={turns}
          message={stageMessage}
          isPending={isPending}
          onStart={() => startTransition(() => void startSession())}
          onGenerate={() => startTransition(() => void generateTurn())}
          onComplete={() => startTransition(() => void completeSession())}
          onClear={() => {
            setResult(undefined);
            setTurns([]);
            setStageMessage(undefined);
          }}
        />
      ) : null}
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
        <small>{character?.description || "설명 없음"}</small>
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

function DebateResult({
  result,
  turns,
  message,
  isPending,
  onStart,
  onGenerate,
  onComplete,
  onClear,
}: {
  result: DebateSession;
  turns: DebateTurn[];
  message: string | undefined;
  isPending: boolean;
  onStart: () => void;
  onGenerate: () => void;
  onComplete: () => void;
  onClear: () => void;
}) {
  const canStart = result.status === "CREATED";
  const canGenerate = result.status === "RUNNING";
  const canComplete = result.status === "RUNNING";

  return (
    <section className="debate-result" aria-live="polite">
      <header>
        <div>
          <p className="eyebrow">SESSION CREATED</p>
          <h3>{result.topicTitle}</h3>
        </div>
        <span>{result.status ?? "CREATED"}</span>
      </header>
      <p>{result.topicDescription}</p>
      {message ? <p className="result-message">{message}</p> : null}
      <div className="result-actions">
        <button
          className="secondary-button"
          type="button"
          disabled={!canStart || isPending}
          onClick={onStart}
        >
          시작
        </button>
        <button
          className="secondary-button"
          type="button"
          disabled={!canGenerate || isPending}
          onClick={onGenerate}
        >
          다음 턴 생성
        </button>
        <button
          className="secondary-button"
          type="button"
          disabled={!canComplete || isPending}
          onClick={onComplete}
        >
          완료
        </button>
        <button className="ghost-button" type="button" onClick={onClear}>
          세션 지우기
        </button>
      </div>
      <div className="result-participants">
        {(result.participants ?? []).map((participant, index) => (
          <article key={participant.id ?? index}>
            <span>{String((participant.position ?? index) + 1).padStart(2, "0")}</span>
            <div>
              <strong>{participant.name}</strong>
              <small>
                {participant.model} / character #{participant.sourceCharacterId}
              </small>
            </div>
            <dl>
              <dt>페르소나</dt>
              <dd>{formatPersona(participant.persona)}</dd>
            </dl>
          </article>
        ))}
      </div>
      <div className="turn-list">
        {turns.length === 0 ? (
          <p>아직 생성된 턴이 없습니다.</p>
        ) : (
          turns.map((turn) => (
            <article key={turn.id ?? turn.turnIndex}>
              <span>
                R{turn.round} · #{turn.turnIndex} · {turn.participantModel}
              </span>
              <p>{turn.content}</p>
            </article>
          ))
        )}
      </div>
    </section>
  );
}

function formatPersona(value: DebateParticipantSnapshot["persona"]) {
  if (!value) {
    return "미설정";
  }
  const tone = value.voiceStyle?.tone ? ` · ${value.voiceStyle.tone}` : "";
  return `${value.identity ?? "페르소나"} / ${value.debateRole ?? "토론 참가자"}${tone}`;
}
