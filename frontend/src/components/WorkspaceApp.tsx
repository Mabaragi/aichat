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
const CATEGORY_MARKS: Record<string, string> = {
  all: "▦",
  food: "●",
  culture: "◆",
  tech: "◧",
  life: "◐",
  society: "◇",
  fun: "✦",
  other: "○",
  expert: "●",
  critic: "◆",
  creator: "✦",
  storyteller: "◐",
  comedy: "◇",
  utility: "◧",
};

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

  const activeCategoryLabel = useMemo(() => {
    if (activeCategory === ALL_CATEGORY) {
      return "전체";
    }
    return activeCategories.find((category) => category.slug === activeCategory)?.name ?? "선택";
  }, [activeCategories, activeCategory]);
  const resourceLabel = resource === "debates" ? "공개 토론" : "공개 캐릭터";
  const countLabel = activeLoading
    ? "불러오는 중"
    : `${activePage?.totalElements ?? 0}개`;

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
      <div className="platform-backdrop" aria-hidden="true">
        <span className="backdrop-shape backdrop-diamond" />
        <span className="backdrop-shape backdrop-circle" />
      </div>

      <TopNavigation
        user={workspace?.user}
        isAuthenticated={isAuthenticated}
        onLogin={() => router.push("/login")}
        onOpenStudio={openStudio}
      />

      <AnnouncementStrip />

      <section className="explorer-panel" aria-label="공개 탐색" id="public-list">
        <div className="explorer-tools">
          <ResourceTabs resource={resource} onChange={setResource} />
          <form className="search-bar" onSubmit={updateQuery}>
            <input
              name="query"
              defaultValue={query}
              placeholder="검색어를 입력하세요."
              aria-label="검색어"
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
            <h1>{activeCategoryLabel}</h1>
          </div>
          <p aria-label={`${resourceLabel} 수`}>{countLabel}</p>
        </header>

        {resource === "debates" ? (
          <DebateCatalog debates={debatePage?.items ?? []} onCreate={openStudio} />
        ) : (
          <CharacterCatalog characters={characterPage?.items ?? []} onCreate={openStudio} />
        )}
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

function AnnouncementStrip() {
  return (
    <section className="announcement-strip" aria-label="공지">
      <strong>[업데이트]</strong>
      <span>공개 탐색</span>
    </section>
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
        <span aria-hidden="true">{CATEGORY_MARKS.all}</span>
        <span>전체</span>
      </button>
      {categories.map((category) => (
        <button
          key={category.id ?? category.slug}
          className={category.slug === activeCategory ? "active" : ""}
          type="button"
          onClick={() => onSelect(category.slug ?? ALL_CATEGORY)}
        >
          <span aria-hidden="true">
            {CATEGORY_MARKS[category.slug ?? "other"] ?? CATEGORY_MARKS.other}
          </span>
          <span>{category.name}</span>
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
    return <EmptyCatalog message="공개 항목 없음" onCreate={onCreate} />;
  }

  return (
    <div className="catalog-grid">
      {debates.map((debate) => (
        <article className="catalog-card" key={debate.id ?? debate.topicTitle}>
          <CatalogThumbnail
            kind="debate"
            title={debate.topicTitle ?? "토론"}
            meta={debate.category?.name ?? debate.topicCategory ?? "기타"}
            seed={`${debate.id ?? ""}-${debate.topicTitle ?? ""}`}
            badge={debate.status ?? "COMPLETED"}
          />
          <div className="catalog-card-body">
            <h2>{debate.topicTitle}</h2>
            <p>{debate.topicDescription}</p>
            <div className="catalog-meta">
              <span>{debate.category?.name ?? debate.topicCategory ?? "기타"}</span>
              <span>{formatParticipantNames(debate.participants)}</span>
            </div>
          </div>
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
    return <EmptyCatalog message="공개 항목 없음" onCreate={onCreate} />;
  }

  return (
    <div className="catalog-grid">
      {characters.map((character) => (
        <article className="catalog-card" key={character.id ?? character.name}>
          <CatalogThumbnail
            kind="character"
            title={character.name ?? "캐릭터"}
            meta={character.category?.name ?? "기타"}
            seed={`${character.id ?? ""}-${character.name ?? ""}`}
            badge={character.visibility}
          />
          <div className="catalog-card-body">
            <h2>{character.name}</h2>
            <p>{character.description || "설명 없음"}</p>
            <div className="catalog-meta">
              <span>{character.category?.name ?? "기타"}</span>
              <span>{character.visibility}</span>
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}

function CatalogThumbnail({
  kind,
  title,
  meta,
  seed,
  badge,
}: {
  kind: "debate" | "character";
  title: string;
  meta: string;
  seed: string;
  badge: string | undefined;
}) {
  const tone = stableTone(seed);

  return (
    <div
      className={`catalog-thumbnail catalog-thumbnail-${kind} tone-${tone}`}
      aria-label={`${title} 썸네일`}
    >
      <span className="thumbnail-orb thumbnail-orb-one" aria-hidden="true" />
      <span className="thumbnail-orb thumbnail-orb-two" aria-hidden="true" />
      <span className="thumbnail-symbol" aria-hidden="true">
        {makeThumbnailMark(title)}
      </span>
      <span className="thumbnail-meta">{meta}</span>
      {badge ? <span className="thumbnail-badge">{badge}</span> : null}
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
        만들기
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

function formatParticipantNames(participants: DebateSession["participants"]) {
  const names = (participants ?? [])
    .map((participant) => participant.name)
    .filter((name): name is string => Boolean(name));

  if (names.length === 0) {
    return "참가자";
  }
  return names.slice(0, 2).join(" vs ");
}

function stableTone(value: string) {
  let hash = 0;
  for (let index = 0; index < value.length; index += 1) {
    hash = (hash * 31 + value.charCodeAt(index)) % 997;
  }
  return hash % 6;
}

function makeThumbnailMark(value: string) {
  const compact = value.replace(/\s+/g, "");
  return compact.slice(0, 2) || "AI";
}
