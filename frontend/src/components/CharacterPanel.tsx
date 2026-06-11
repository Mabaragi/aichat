"use client";

import type { Category, Character, CharacterCreate, User } from "@/lib/api-types";
import { ApiError, bffFetch, readJson } from "@/lib/bff-fetch";
import { FormEvent, useState, useTransition } from "react";

type CharacterPanelProps = {
  user: User;
  characters: Character[];
  categories: Category[];
  onCreated: (character: Character) => void;
  onLogout: () => void;
};

type PersonaPayload = NonNullable<CharacterCreate["persona"]>;

type PersonaOption = {
  value: string;
  label: string;
  caption: string;
  coreValues?: string[];
  expertise?: string[];
  defaultStance?: string;
  evidenceStyle?: string;
  behavior?: string[];
  rhetoricalStyle?: string;
  tone?: string;
  exampleLine?: string;
};

type PersonaState = {
  role: string;
  evidence: string;
  rebuttal: string;
  voice: string;
  personaBrief: string;
  analysis: number;
  pressure: number;
  technicality: number;
  sentenceLength: number;
  flexibility: number;
  boundaries: string[];
};

type CardField = "role" | "evidence" | "rebuttal" | "voice";
type SliderField =
  | "analysis"
  | "pressure"
  | "technicality"
  | "sentenceLength"
  | "flexibility";

const ROLE_PRESETS: PersonaOption[] = [
  {
    value: "data-analyst",
    label: "데이터 분석가",
    caption: "숫자와 패턴",
    coreValues: ["실증성", "재현성"],
    expertise: ["통계", "데이터 해석"],
    defaultStance: "감보다 측정 가능한 근거를 먼저 확인한다.",
    exampleLine: "그 주장에는 방향보다 측정 기준이 먼저 필요합니다.",
  },
  {
    value: "ethicist",
    label: "윤리학자",
    caption: "공정성과 피해",
    coreValues: ["공정성", "책임"],
    expertise: ["윤리 원칙", "사회적 영향"],
    defaultStance: "결과뿐 아니라 절차와 피해 가능성을 함께 본다.",
    exampleLine: "효율이 높아도 누가 비용을 떠안는지는 따져야 합니다.",
  },
  {
    value: "citizen",
    label: "시민 대표",
    caption: "생활감과 접근성",
    coreValues: ["생활감", "접근성"],
    expertise: ["일상 경험", "사용자 관점"],
    defaultStance: "현장에서 실제로 받아들여질 수 있는지를 먼저 본다.",
    exampleLine: "현장에서 쓰기 어렵다면 좋은 정책이어도 오래가기 힘듭니다.",
  },
  {
    value: "lawyer",
    label: "법률가",
    caption: "원칙과 책임",
    coreValues: ["규칙", "책임 소재"],
    expertise: ["법적 기준", "권리와 의무"],
    defaultStance: "원칙과 예외, 책임 범위를 구분한다.",
    exampleLine: "쟁점은 가능 여부가 아니라 책임이 어디에 놓이는가입니다.",
  },
  {
    value: "futurist",
    label: "미래학자",
    caption: "장기 변화",
    coreValues: ["장기 영향", "변화 가능성"],
    expertise: ["미래 시나리오", "기술 변화"],
    defaultStance: "단기 효율보다 장기 변화의 방향을 함께 본다.",
    exampleLine: "지금의 불편보다 다음 단계의 구조 변화를 봐야 합니다.",
  },
  {
    value: "skeptic",
    label: "회의적 반대자",
    caption: "반례와 리스크",
    coreValues: ["검증", "위험 관리"],
    expertise: ["반례 탐색", "리스크 분석"],
    defaultStance: "좋은 의도보다 실패 조건과 부작용을 먼저 본다.",
    exampleLine: "선의는 알겠지만, 실패했을 때의 비용이 빠져 있습니다.",
  },
  {
    value: "mediator",
    label: "중재자",
    caption: "쟁점 정리",
    coreValues: ["균형", "상호 이해"],
    expertise: ["갈등 조정", "쟁점 정리"],
    defaultStance: "양쪽 주장의 강점과 약점을 같이 정리한다.",
    exampleLine: "두 주장 모두 맞는 지점과 놓친 지점을 나눠 보겠습니다.",
  },
  {
    value: "provocateur",
    label: "도발적 비판자",
    caption: "전제 흔들기",
    coreValues: ["긴장감", "전제 검증"],
    expertise: ["논점 흔들기", "반문"],
    defaultStance: "당연해 보이는 전제를 일부러 의심한다.",
    exampleLine: "그 결론은 멋지지만, 전제가 너무 편리합니다.",
  },
];

const EVIDENCE_PRESETS: PersonaOption[] = [
  {
    value: "research",
    label: "통계/연구",
    caption: "데이터 우선",
    evidenceStyle: "통계, 연구 결과, 비교 데이터를 우선한다.",
  },
  {
    value: "history",
    label: "역사 사례",
    caption: "반복 패턴",
    evidenceStyle: "역사적 사례와 반복 패턴을 근거로 든다.",
  },
  {
    value: "ethics",
    label: "윤리 원칙",
    caption: "권리와 책임",
    evidenceStyle: "권리, 책임, 피해 최소화 원칙을 중심으로 판단한다.",
  },
  {
    value: "cost",
    label: "현실 비용",
    caption: "실행 조건",
    evidenceStyle: "실행 비용, 운영 부담, 부작용을 중심으로 판단한다.",
  },
  {
    value: "experience",
    label: "개인 경험",
    caption: "구체 상황",
    evidenceStyle: "생활 경험과 구체적 상황 예시를 근거로 든다.",
  },
  {
    value: "balanced",
    label: "균형형",
    caption: "데이터+사례",
    evidenceStyle: "데이터, 원칙, 사례를 균형 있게 사용한다.",
  },
];

const REBUTTAL_PRESETS: PersonaOption[] = [
  {
    value: "calm-analysis",
    label: "차분한 분석",
    caption: "구조화",
    behavior: ["상대 주장을 구조화한 뒤 약한 연결고리를 찾는다."],
    rhetoricalStyle: "요약과 구조적 반박 중심",
  },
  {
    value: "sharp-question",
    label: "날카로운 질문",
    caption: "핵심 반문",
    behavior: ["상대 주장의 숨은 전제를 질문으로 드러낸다."],
    rhetoricalStyle: "짧은 질문과 핵심 반문 중심",
  },
  {
    value: "premise-attack",
    label: "전제 공격",
    caption: "가정 검증",
    behavior: ["결론보다 전제의 타당성을 먼저 검토한다."],
    rhetoricalStyle: "전제 분해와 반례 제시 중심",
  },
  {
    value: "mediation",
    label: "중재",
    caption: "남는 쟁점",
    behavior: ["서로 양립 가능한 지점과 남는 쟁점을 분리한다."],
    rhetoricalStyle: "비교와 조율 중심",
  },
  {
    value: "satire",
    label: "풍자",
    caption: "짧은 비틀기",
    behavior: ["과장된 비유로 논리의 허점을 드러낸다."],
    rhetoricalStyle: "짧은 풍자와 반전 중심",
  },
  {
    value: "socratic",
    label: "소크라테스식 질문",
    caption: "연속 질문",
    behavior: ["연속 질문으로 스스로 모순을 확인하게 한다."],
    rhetoricalStyle: "단계적 질문 중심",
  },
];

const VOICE_PRESETS: PersonaOption[] = [
  { value: "calm", label: "차분함", caption: "낮은 온도", tone: "차분함" },
  { value: "direct", label: "직설적", caption: "짧고 선명", tone: "직설적" },
  { value: "friendly", label: "친근함", caption: "부드러운 설득", tone: "친근함" },
  { value: "professional", label: "전문적", caption: "정돈된 어휘", tone: "전문적" },
];

const BOUNDARY_OPTIONS = [
  "인신공격하지 않는다.",
  "허위 사실을 단정하지 않는다.",
  "상대 주장을 왜곡하지 않는다.",
  "민감 속성에 대한 고정관념을 사용하지 않는다.",
  "출처 없는 수치를 남발하지 않는다.",
] as const;

const DEFAULT_PERSONA_STATE: PersonaState = {
  role: ROLE_PRESETS[0].value,
  evidence: EVIDENCE_PRESETS[0].value,
  rebuttal: REBUTTAL_PRESETS[0].value,
  voice: VOICE_PRESETS[0].value,
  personaBrief: "",
  analysis: 4,
  pressure: 3,
  technicality: 3,
  sentenceLength: 3,
  flexibility: 4,
  boundaries: [...BOUNDARY_OPTIONS],
};

const QUICK_TUNES = [
  "더 차분하게",
  "더 날카롭게",
  "더 짧게",
  "근거를 더 많이",
  "중립성을 더 높게",
] as const;

export function CharacterPanel({
  user,
  characters,
  categories,
  onCreated,
  onLogout,
}: CharacterPanelProps) {
  const [expanded, setExpanded] = useState(characters.length === 0);
  const [draftName, setDraftName] = useState("");
  const [personaState, setPersonaState] = useState<PersonaState>(
    DEFAULT_PERSONA_STATE,
  );
  const [error, setError] = useState<string>();
  const [isPending, startTransition] = useTransition();
  const previewPersona = buildPersona(personaState, draftName);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);

    const name = draftName.trim();
    const description = String(data.get("description") ?? "").trim();

    if (!name) {
      setError("캐릭터 이름을 입력해 주세요.");
      return;
    }

    const persona = buildPersona(personaState, name);
    if (persona.boundaries.mustNotDo.length === 0) {
      setError("금지 행동을 하나 이상 선택해 주세요.");
      return;
    }

    const payload: CharacterCreate = {
      name,
      category: String(data.get("category") ?? "other").trim() || "other",
      description: description || persona.identity,
      persona,
      visibility: String(data.get("visibility") ?? "PRIVATE") as
        | "PUBLIC"
        | "PRIVATE",
    };

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
      setDraftName("");
      setPersonaState(DEFAULT_PERSONA_STATE);
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

  function updateCard(field: CardField, value: string) {
    setPersonaState((current) => ({ ...current, [field]: value }));
  }

  function updateSlider(field: SliderField, value: number) {
    setPersonaState((current) => ({ ...current, [field]: value }));
  }

  function updateBoundary(boundary: string, checked: boolean) {
    setPersonaState((current) => ({
      ...current,
      boundaries: checked
        ? [...new Set([...current.boundaries, boundary])]
        : current.boundaries.filter((item) => item !== boundary),
    }));
  }

  function applyQuickTune(label: (typeof QUICK_TUNES)[number]) {
    setPersonaState((current) => {
      if (label === "더 차분하게") {
        return {
          ...current,
          voice: "calm",
          pressure: Math.max(1, current.pressure - 1),
        };
      }
      if (label === "더 날카롭게") {
        return {
          ...current,
          rebuttal: "sharp-question",
          pressure: Math.min(5, current.pressure + 1),
        };
      }
      if (label === "더 짧게") {
        return {
          ...current,
          sentenceLength: Math.max(1, current.sentenceLength - 1),
        };
      }
      if (label === "근거를 더 많이") {
        return {
          ...current,
          evidence: "research",
          analysis: Math.min(5, current.analysis + 1),
        };
      }
      return {
        ...current,
        role: "mediator",
        rebuttal: "mediation",
        pressure: Math.max(1, current.pressure - 1),
      };
    });
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
          <p className="empty-copy">공개 토론에 쓸 캐릭터를 먼저 만드세요.</p>
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
        <form
          className="character-form persona-builder field-reveal"
          onSubmit={handleSubmit}
        >
          <div className="persona-builder-grid">
            <div className="persona-flow">
              <section className="persona-step">
                <span className="step-marker">1</span>
                <div className="persona-step-body">
                  <h4>한 줄</h4>
                  <div className="persona-basic-grid">
                    <label>
                      이름
                      <input
                        name="name"
                        maxLength={50}
                        placeholder="합리적 미식가"
                        required
                        value={draftName}
                        onChange={(event) => setDraftName(event.target.value)}
                      />
                    </label>
                    <label>
                      카테고리
                      <select
                        name="category"
                        defaultValue={categories[0]?.slug ?? "other"}
                      >
                        {categories.map((category) => (
                          <option
                            key={category.id ?? category.slug}
                            value={category.slug}
                          >
                            {category.name}
                          </option>
                        ))}
                      </select>
                    </label>
                    <label className="wide-field">
                      어떤 캐릭터인가요?
                      <input
                        name="personaBrief"
                        maxLength={180}
                        placeholder="감정적 선동보다 데이터와 제도 설계를 보는 정책 분석가"
                        value={personaState.personaBrief}
                        onChange={(event) =>
                          setPersonaState((current) => ({
                            ...current,
                            personaBrief: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <label className="wide-field">
                      공개 설명
                      <textarea
                        name="description"
                        maxLength={1000}
                        rows={2}
                        placeholder="카드와 목록에 보이는 짧은 설명"
                      />
                    </label>
                  </div>
                </div>
              </section>

              <section className="persona-step">
                <span className="step-marker">2</span>
                <div className="persona-step-body">
                  <h4>카드 선택</h4>
                  <PersonaCardGroup
                    legend="토론 역할"
                    name="role"
                    options={ROLE_PRESETS}
                    selected={personaState.role}
                    onChange={(value) => updateCard("role", value)}
                  />
                  <PersonaCardGroup
                    legend="근거 스타일"
                    name="evidence"
                    options={EVIDENCE_PRESETS}
                    selected={personaState.evidence}
                    onChange={(value) => updateCard("evidence", value)}
                  />
                  <PersonaCardGroup
                    legend="반박 스타일"
                    name="rebuttal"
                    options={REBUTTAL_PRESETS}
                    selected={personaState.rebuttal}
                    onChange={(value) => updateCard("rebuttal", value)}
                  />
                  <PersonaCardGroup
                    legend="말투"
                    name="voice"
                    options={VOICE_PRESETS}
                    selected={personaState.voice}
                    onChange={(value) => updateCard("voice", value)}
                  />
                </div>
              </section>

              <section className="persona-step">
                <span className="step-marker">3</span>
                <div className="persona-step-body">
                  <h4>고급 조정</h4>
                  <div className="persona-slider-grid">
                    <PersonaSlider
                      label="감정-분석"
                      minLabel="감정적"
                      maxLabel="분석적"
                      value={personaState.analysis}
                      onChange={(value) => updateSlider("analysis", value)}
                    />
                    <PersonaSlider
                      label="반박 압력"
                      minLabel="협력적"
                      maxLabel="날카롭게 반박"
                      value={personaState.pressure}
                      onChange={(value) => updateSlider("pressure", value)}
                    />
                    <PersonaSlider
                      label="전문성"
                      minLabel="대중적"
                      maxLabel="전문적"
                      value={personaState.technicality}
                      onChange={(value) => updateSlider("technicality", value)}
                    />
                    <PersonaSlider
                      label="문장 길이"
                      minLabel="간결함"
                      maxLabel="장문 설명"
                      value={personaState.sentenceLength}
                      onChange={(value) => updateSlider("sentenceLength", value)}
                    />
                    <PersonaSlider
                      label="입장 수정"
                      minLabel="고정 입장"
                      maxLabel="근거에 따라 수정"
                      value={personaState.flexibility}
                      onChange={(value) => updateSlider("flexibility", value)}
                    />
                  </div>
                </div>
              </section>

              <section className="persona-step">
                <span className="step-marker">4</span>
                <div className="persona-step-body">
                  <h4>금지 행동</h4>
                  <fieldset className="boundary-fieldset">
                    <legend>선택</legend>
                    {BOUNDARY_OPTIONS.map((option) => (
                      <label className="check-option" key={option}>
                        <input
                          name="boundaries"
                          type="checkbox"
                          value={option}
                          checked={personaState.boundaries.includes(option)}
                          onChange={(event) =>
                            updateBoundary(option, event.target.checked)
                          }
                        />
                        <span>{option}</span>
                      </label>
                    ))}
                  </fieldset>
                  <label>
                    공개 범위
                    <select name="visibility" defaultValue="PRIVATE">
                      <option value="PRIVATE">비공개</option>
                      <option value="PUBLIC">공개</option>
                    </select>
                  </label>
                </div>
              </section>
            </div>

            <PersonaPreview
              persona={previewPersona}
              onQuickTune={applyQuickTune}
            />
          </div>

          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button wide" type="submit" disabled={isPending}>
            {isPending ? "저장 중" : "캐릭터 저장"}
          </button>
        </form>
      ) : null}
    </section>
  );
}

function PersonaCardGroup({
  legend,
  name,
  options,
  selected,
  onChange,
}: {
  legend: string;
  name: CardField;
  options: PersonaOption[];
  selected: string;
  onChange: (value: string) => void;
}) {
  return (
    <fieldset className="persona-card-group">
      <legend>{legend}</legend>
      <div className="persona-card-grid">
        {options.map((option) => (
          <label className="persona-choice-card" key={option.value}>
            <input
              type="radio"
              name={name}
              value={option.value}
              checked={selected === option.value}
              onChange={() => onChange(option.value)}
            />
            <span>{option.label}</span>
            <small>{option.caption}</small>
          </label>
        ))}
      </div>
    </fieldset>
  );
}

function PersonaSlider({
  label,
  minLabel,
  maxLabel,
  value,
  onChange,
}: {
  label: string;
  minLabel: string;
  maxLabel: string;
  value: number;
  onChange: (value: number) => void;
}) {
  return (
    <label className="persona-slider">
      <span>
        <strong>{label}</strong>
        <small>{value}/5</small>
      </span>
      <input
        type="range"
        min={1}
        max={5}
        value={value}
        onChange={(event) => onChange(Number(event.target.value))}
      />
      <span className="persona-scale">
        <small>{minLabel}</small>
        <small>{maxLabel}</small>
      </span>
    </label>
  );
}

function PersonaPreview({
  persona,
  onQuickTune,
}: {
  persona: PersonaPayload;
  onQuickTune: (label: (typeof QUICK_TUNES)[number]) => void;
}) {
  return (
    <aside className="persona-preview" aria-label="페르소나 미리보기">
      <div className="persona-preview-head">
        <p className="eyebrow">PREVIEW</p>
        <h4>{persona.debateRole}</h4>
      </div>
      <dl className="persona-preview-list">
        <div>
          <dt>정체성</dt>
          <dd>{persona.identity}</dd>
        </div>
        <div>
          <dt>가치</dt>
          <dd>{persona.coreValues.join(", ")}</dd>
        </div>
        <div>
          <dt>근거</dt>
          <dd>{persona.evidenceStyle}</dd>
        </div>
        <div>
          <dt>반박</dt>
          <dd>{persona.debateBehavior.join(" ")}</dd>
        </div>
        <div>
          <dt>말투</dt>
          <dd>
            {persona.voiceStyle.tone} · {persona.voiceStyle.sentenceLength}
          </dd>
        </div>
      </dl>
      <div className="persona-sample">
        <span>[요약]</span>
        <p>상대 주장을 먼저 정리한다.</p>
        <span>[핵심 반박]</span>
        <p>{persona.exampleLines[0] ?? "전제와 근거를 분리해 반박한다."}</p>
        <span>[입장 상태]</span>
        <p>{persona.boundaries.mustDo.at(-1) ?? "근거에 따라 판단한다."}</p>
      </div>
      <div className="persona-tune-row" aria-label="빠른 조정">
        {QUICK_TUNES.map((label) => (
          <button
            className="persona-tune-button"
            key={label}
            type="button"
            onClick={() => onQuickTune(label)}
          >
            {label}
          </button>
        ))}
      </div>
    </aside>
  );
}

function buildPersona(state: PersonaState, characterName: string): PersonaPayload {
  const role = findOption(ROLE_PRESETS, state.role);
  const evidence = findOption(EVIDENCE_PRESETS, state.evidence);
  const rebuttal = findOption(REBUTTAL_PRESETS, state.rebuttal);
  const voice = findOption(VOICE_PRESETS, state.voice);
  const identity = state.personaBrief.trim() || characterName.trim() || role.label;
  const pressureRule = pressureBehavior(state.pressure);
  const flexibilityRule = flexibilityBehavior(state.flexibility);

  return {
    identity,
    debateRole: role.label,
    coreValues: role.coreValues ?? ["사실성"],
    expertise: role.expertise ?? [],
    defaultStance: compactText(role.defaultStance, flexibilityRule.stance),
    evidenceStyle: compactText(
      evidence.evidenceStyle,
      analysisEvidence(state.analysis),
      technicalEvidence(state.technicality),
    ),
    debateBehavior: compactList([
      ...(rebuttal.behavior ?? []),
      pressureRule,
      flexibilityRule.behavior,
    ]),
    voiceStyle: {
      tone: compactText(voice.tone ?? voice.label, pressureTone(state.pressure)),
      sentenceLength: sentenceLengthLabel(state.sentenceLength),
      rhetoricalStyle: rebuttal.rhetoricalStyle ?? "질문과 구조적 반박 중심",
      signaturePhrases: [],
    },
    boundaries: {
      mustDo: compactList([
        "상대 주장을 먼저 요약한다.",
        "불확실한 사실은 단정하지 않는다.",
        flexibilityRule.mustDo,
      ]),
      mustNotDo: state.boundaries,
    },
    exampleLines: compactList([
      role.exampleLine,
      exampleLine(evidence.label, rebuttal.label),
    ]),
  };
}

function findOption<T extends PersonaOption>(options: readonly T[], value: string) {
  return options.find((option) => option.value === value) ?? options[0];
}

function compactText(...parts: Array<string | undefined>) {
  return parts.filter(Boolean).join(" ");
}

function compactList(parts: Array<string | undefined>) {
  return parts.filter(Boolean) as string[];
}

function pressureBehavior(value: number) {
  if (value >= 4) {
    return "상대 주장의 핵심 전제를 강하게 압박한다.";
  }
  if (value <= 2) {
    return "상대가 받아들일 수 있는 공통 기준부터 확인한다.";
  }
  return "상대 주장과 내 반박의 차이를 명확히 나눈다.";
}

function pressureTone(value: number) {
  if (value >= 4) {
    return "날카로움";
  }
  if (value <= 2) {
    return "협력적";
  }
  return undefined;
}

function analysisEvidence(value: number) {
  if (value >= 4) {
    return "판단 전에 기준과 변수부터 분리한다.";
  }
  if (value <= 2) {
    return "감정과 현장 반응도 근거로 고려한다.";
  }
  return undefined;
}

function technicalEvidence(value: number) {
  if (value >= 4) {
    return "전문 용어는 쓰되 필요한 만큼만 풀어 설명한다.";
  }
  if (value <= 2) {
    return "비전문가도 이해할 수 있는 사례를 우선한다.";
  }
  return undefined;
}

function sentenceLengthLabel(value: number) {
  if (value >= 4) {
    return "긴 편";
  }
  if (value <= 2) {
    return "짧음";
  }
  return "중간";
}

function flexibilityBehavior(value: number) {
  if (value >= 4) {
    return {
      stance: "강한 근거가 나오면 입장을 일부 수정한다.",
      behavior: "반례가 강하면 기존 입장의 조건을 좁힌다.",
      mustDo: "강한 근거가 나오면 입장을 일부 수정한다.",
    };
  }
  if (value <= 2) {
    return {
      stance: "핵심 가치와 충돌하면 쉽게 입장을 바꾸지 않는다.",
      behavior: "반례를 인정하되 핵심 기준은 유지한다.",
      mustDo: "반례와 핵심 입장을 구분해 말한다.",
    };
  }
  return {
    stance: "근거 수준에 따라 입장 강도를 조절한다.",
    behavior: "상대 근거의 강도에 따라 반박 범위를 조절한다.",
    mustDo: "근거 수준에 따라 입장 강도를 표시한다.",
  };
}

function exampleLine(evidenceLabel: string, rebuttalLabel: string) {
  return `${evidenceLabel} 기준으로 보면, ${rebuttalLabel}이 먼저 필요합니다.`;
}
