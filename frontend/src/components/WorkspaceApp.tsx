"use client";

import { CharacterPanel } from "@/components/CharacterPanel";
import { DebateComposer } from "@/components/DebateComposer";
import type {
  Category,
  Character,
  DebateSession,
  PublicCharacterPage,
  PublicDebatePage,
  User,
  WorkspaceData,
} from "@/lib/api-types";
import { bffFetch, readJson, workspaceFetcher } from "@/lib/bff-fetch";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useDeferredValue, useMemo, useState, useTransition } from "react";
import useSWR from "swr";

type Resource = "debates" | "characters";

const ALL_CATEGORY = "all";

export function WorkspaceApp() {
  const router = useRouter();
  const [resource, setResource] = useState<Resource>("debates");
  const [query, setQuery] = useState("");
  const [debateCategory, setDebateCategory] = useState(ALL_CATEGORY);
  const [characterCategory, setCharacterCategory] = useState(ALL_CATEGORY);
  const [studioOpen, setStudioOpen] = useState(false);
  const [isPending, startTransition] = useTransition();
  const deferredQuery = useDeferredValue(query.trim());

  const { data: workspace, error: workspaceError, mutate } = useSWR<WorkspaceData>(
    "/api/workspace",
    workspaceFetcher,
    {
      shouldRetryOnError: false,
      revalidateOnFocus: false,
    },
  );
  const { data: debateCategories = [] } = useSWR<Category[]>(
    "/api/public/categories?scope=DEBATE",
    jsonFetcher,
  );
  const { data: characterCategories = [] } = useSWR<Category[]>(
    "/api/public/categories?scope=CHARACTER",
    jsonFetcher,
  );

  const selectedDebateCategory =
    debateCategory === ALL_CATEGORY ? undefined : debateCategory;
  const selectedCharacterCategory =
    characterCategory === ALL_CATEGORY ? undefined : characterCategory;
  const debateListUrl = publicListUrl(
    "/api/public/debate-sessions",
    deferredQuery,
    selectedDebateCategory,
  );
  const characterListUrl = publicListUrl(
    "/api/public/characters",
    deferredQuery,
    selectedCharacterCategory,
  );

  const { data: debatePage, isLoading: debatesLoading } = useSWR<PublicDebatePage>(
    debateListUrl,
    jsonFetcher,
    { keepPreviousData: true },
  );
  const { data: characterPage, isLoading: charactersLoading } =
    useSWR<PublicCharacterPage>(characterListUrl, jsonFetcher, {
      keepPreviousData: true,
    });

  const isAuthenticated = Boolean(workspace && !workspaceError);
  const activeCategories = resource === "debates" ? debateCategories : characterCategories;
  const activeCategory = resource === "debates" ? debateCategory : characterCategory;
  const activePage = resource === "debates" ? debatePage : characterPage;
  const activeLoading = resource === "debates" ? debatesLoading : charactersLoading;

  const categoryDescription = useMemo(() => {
    if (activeCategory === ALL_CATEGORY) {
      return resource === "debates"
        ? "완료된 공개 토론 전체"
        : "공개 캐릭터 전체";
    }
    return activeCategories.find((category) => category.slug === activeCategory)?.name ?? "선택됨";
  }, [activeCategories, activeCategory, resource]);

  function updateQuery(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    startTransition(() => {
      setQuery(String(data.get("query") ?? ""));
    });
  }

  function openStudio() {
    if (!isAuthenticated) {
      router.push("/login");
      return;
    }
    setStudioOpen(true);
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
    setStudioOpen(false);
    await mutate(undefined, { revalidate: false });
  }

  return (
    <main className="platform-shell">
      <TopNavigation
        user={workspace?.user}
        isAuthenticated={isAuthenticated}
        onLogin={() => router.push("/login")}
        onOpenStudio={openStudio}
      />

      <section className="arena-heading">
        <div className="geometric-shape shape-one" />
        <div className="geometric-shape shape-two" />
        <p className="eyebrow">PUBLIC ARENA</p>
        <h1>완료된 AI 토론과 공개 캐릭터를 바로 탐색합니다.</h1>
        <p>
          검색어와 카테고리로 공개된 결과만 좁혀보고, 로그인하면 같은 화면에서
          내 캐릭터와 토론을 만들 수 있습니다.
        </p>
      </section>

      <section className="explorer-panel" aria-label="공개 탐색">
        <div className="explorer-tools">
          <ResourceTabs resource={resource} onChange={setResource} />
          <form className="search-bar" onSubmit={updateQuery}>
            <input
              name="query"
              defaultValue={query}
              placeholder={
                resource === "debates"
                  ? "토론 제목, 설명, 참가자 이름 검색"
                  : "캐릭터 이름 또는 설명 검색"
              }
            />
            <button className="primary-button" type="submit" disabled={isPending}>
              검색
            </button>
          </form>
        </div>

        <CategoryRail
          categories={activeCategories}
          activeCategory={activeCategory}
          onSelect={(slug) =>
            resource === "debates"
              ? setDebateCategory(slug)
              : setCharacterCategory(slug)
          }
        />

        <header className="section-title-row">
          <div>
            <p className="eyebrow">{resource === "debates" ? "DEBATES" : "CHARACTERS"}</p>
            <h2>{categoryDescription}</h2>
          </div>
          <p>
            {activeLoading
              ? "불러오는 중"
              : `${activePage?.totalElements ?? 0}개의 공개 항목`}
          </p>
        </header>

        {resource === "debates" ? (
          <DebateCatalog debates={debatePage?.items ?? []} onCreate={openStudio} />
        ) : (
          <CharacterCatalog characters={characterPage?.items ?? []} onCreate={openStudio} />
        )}
      </section>

      <section className="studio-band" aria-labelledby="studio-title">
        <div>
          <p className="eyebrow">MY STUDIO</p>
          <h2 id="studio-title">내 캐릭터로 공개될 수 있는 토론을 만듭니다.</h2>
          <p>
            PUBLIC 토론은 완료된 뒤 공개 목록에 노출됩니다. 생성 직후에는 내
            작업공간에서만 이어서 진행합니다.
          </p>
        </div>
        <button className="primary-button" type="button" onClick={openStudio}>
          {isAuthenticated ? "제작 스튜디오 열기" : "로그인하고 만들기"}
        </button>
      </section>

      {studioOpen && workspace ? (
        <section className="studio-panel" aria-label="제작 스튜디오">
          <div className="studio-panel-header">
            <div>
              <p className="eyebrow">SIGNED IN</p>
              <h2>{workspace.user.nickname ?? "토론가"}님의 작업공간</h2>
            </div>
            <button
              className="ghost-button"
              type="button"
              onClick={() => setStudioOpen(false)}
            >
              닫기
            </button>
          </div>
          <div className="studio-layout">
            <CharacterPanel
              user={workspace.user}
              characters={workspace.characters}
              categories={characterCategories}
              onCreated={handleCharacterCreated}
              onLogout={handleLogout}
            />
            <DebateComposer
              characters={workspace.characters}
              categories={debateCategories}
            />
          </div>
        </section>
      ) : null}
    </main>
  );
}

type TopNavigationProps = {
  user: User | undefined;
  isAuthenticated: boolean;
  onLogin: () => void;
  onOpenStudio: () => void;
};

function TopNavigation({
  user,
  isAuthenticated,
  onLogin,
  onOpenStudio,
}: TopNavigationProps) {
  return (
    <header className="top-navigation">
      <Link className="brand-mark" href="/" aria-label="AI Debate Arena 홈">
        <span>AI</span>
        Debate Arena
      </Link>
      <nav aria-label="주요 메뉴">
        <a href="#public-list">공개 탐색</a>
        <button type="button" onClick={onOpenStudio}>
          만들기
        </button>
      </nav>
      <div className="nav-actions">
        {isAuthenticated ? (
          <span className="user-chip">{user?.nickname ?? "토론가"}</span>
        ) : null}
        <button
          className={isAuthenticated ? "secondary-button" : "primary-button"}
          type="button"
          onClick={isAuthenticated ? onOpenStudio : onLogin}
        >
          {isAuthenticated ? "내 작업공간" : "로그인"}
        </button>
      </div>
    </header>
  );
}

function ResourceTabs({
  resource,
  onChange,
}: {
  resource: Resource;
  onChange: (resource: Resource) => void;
}) {
  return (
    <div className="resource-tabs" role="tablist" aria-label="탐색 대상">
      <button
        type="button"
        role="tab"
        aria-selected={resource === "debates"}
        className={resource === "debates" ? "active" : ""}
        onClick={() => onChange("debates")}
      >
        공개 토론
      </button>
      <button
        type="button"
        role="tab"
        aria-selected={resource === "characters"}
        className={resource === "characters" ? "active" : ""}
        onClick={() => onChange("characters")}
      >
        공개 캐릭터
      </button>
    </div>
  );
}

function CategoryRail({
  categories,
  activeCategory,
  onSelect,
}: {
  categories: Category[];
  activeCategory: string;
  onSelect: (slug: string) => void;
}) {
  return (
    <section className="category-rail" aria-label="카테고리">
      <button
        className={activeCategory === ALL_CATEGORY ? "active" : ""}
        type="button"
        onClick={() => onSelect(ALL_CATEGORY)}
      >
        전체
      </button>
      {categories.map((category) => (
        <button
          key={category.id ?? category.slug}
          className={category.slug === activeCategory ? "active" : ""}
          type="button"
          onClick={() => onSelect(category.slug ?? ALL_CATEGORY)}
        >
          {category.name}
        </button>
      ))}
    </section>
  );
}

function DebateCatalog({
  debates,
  onCreate,
}: {
  debates: DebateSession[];
  onCreate: () => void;
}) {
  if (debates.length === 0) {
    return <EmptyCatalog message="조건에 맞는 공개 토론이 없습니다." onCreate={onCreate} />;
  }

  return (
    <div className="debate-grid" id="public-list">
      {debates.map((debate) => (
        <article className="debate-card" key={debate.id}>
          <div className="debate-card-top">
            <span>{debate.category?.name ?? debate.topicCategory ?? "기타"}</span>
            <strong>{debate.status}</strong>
          </div>
          <div className="debate-card-mark" />
          <h3>{debate.topicTitle}</h3>
          <p>{debate.topicDescription}</p>
          <div className="mini-participants">
            {(debate.participants ?? []).map((participant) => (
              <span key={participant.id ?? participant.position}>
                {participant.name}
              </span>
            ))}
          </div>
          <dl className="stat-line">
            <div>
              <dt>라운드</dt>
              <dd>{debate.currentRound ?? 0}/{debate.maxRounds ?? 0}</dd>
            </div>
            <div>
              <dt>형식</dt>
              <dd>{debate.format}</dd>
            </div>
            <div>
              <dt>완료</dt>
              <dd>{formatDate(debate.endedAt)}</dd>
            </div>
          </dl>
        </article>
      ))}
    </div>
  );
}

function CharacterCatalog({
  characters,
  onCreate,
}: {
  characters: Character[];
  onCreate: () => void;
}) {
  if (characters.length === 0) {
    return <EmptyCatalog message="조건에 맞는 공개 캐릭터가 없습니다." onCreate={onCreate} />;
  }

  return (
    <div className="character-catalog" id="public-list">
      {characters.map((character) => (
        <article className="public-character-card" key={character.id}>
          <span>{character.category?.name ?? "기타"}</span>
          <h3>{character.name}</h3>
          <p>{character.description || "설명 없음"}</p>
          <small>{character.visibility}</small>
        </article>
      ))}
    </div>
  );
}

function EmptyCatalog({
  message,
  onCreate,
}: {
  message: string;
  onCreate: () => void;
}) {
  return (
    <div className="empty-catalog">
      <p>{message}</p>
      <button className="secondary-button" type="button" onClick={onCreate}>
        직접 만들기
      </button>
    </div>
  );
}

async function jsonFetcher<T>(url: string): Promise<T> {
  return readJson<T>(await bffFetch(url));
}

function publicListUrl(base: string, query: string, category: string | undefined) {
  const params = new URLSearchParams({ page: "0", size: "20" });
  if (query) {
    params.set("query", query);
  }
  if (category) {
    params.set("category", category);
  }
  return `${base}?${params.toString()}`;
}

function formatDate(value: string | undefined) {
  if (!value) {
    return "-";
  }
  return value.slice(0, 10);
}
