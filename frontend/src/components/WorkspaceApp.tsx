"use client";

import { AuthPanel } from "@/components/AuthPanel";
import { CharacterPanel } from "@/components/CharacterPanel";
import { DebateComposer } from "@/components/DebateComposer";
import type {
  Character,
  PlatformCategory,
  PlatformDebateCard,
  User,
  WorkspaceData,
} from "@/lib/api-types";
import { bffFetch, workspaceFetcher } from "@/lib/bff-fetch";
import {
  characterSpotlights,
  platformCategories,
  platformDebates,
  recentPublicSessions,
} from "@/lib/platform-mock";
import type { CSSProperties } from "react";
import { useDeferredValue, useState, useTransition } from "react";
import useSWR from "swr";

const FEATURED_DEBATE = platformDebates[0];

export function WorkspaceApp() {
  const [activeCategory, setActiveCategory] = useState("all");
  const [authOpen, setAuthOpen] = useState(false);
  const [studioOpen, setStudioOpen] = useState(false);
  const [isCategoryPending, startCategoryTransition] = useTransition();
  const deferredCategory = useDeferredValue(activeCategory);

  const { data, error, isLoading, mutate } = useSWR<WorkspaceData>(
    "/api/workspace",
    workspaceFetcher,
    {
      shouldRetryOnError: false,
      revalidateOnFocus: false,
    },
  );

  const isAuthenticated = Boolean(data && !error);
  const selectedCategory =
    platformCategories.find((category) => category.id === deferredCategory) ??
    platformCategories[0];
  const visibleDebates =
    deferredCategory === "all"
      ? platformDebates
      : platformDebates.filter((debate) => debate.categoryId === deferredCategory);

  function selectCategory(categoryId: string) {
    startCategoryTransition(() => {
      setActiveCategory(categoryId);
    });
  }

  function openStudio() {
    if (!isAuthenticated) {
      setAuthOpen(true);
      return;
    }
    setStudioOpen(true);
  }

  function handleAuthenticated(user: User) {
    setAuthOpen(false);
    setStudioOpen(true);
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
    setStudioOpen(false);
    await mutate(undefined, { revalidate: false });
  }

  return (
    <main className="platform-shell">
      <TopNavigation
        isAuthenticated={isAuthenticated}
        user={data?.user}
        onLogin={() => setAuthOpen(true)}
        onOpenStudio={openStudio}
      />

      <HeroSection
        featuredDebate={FEATURED_DEBATE}
        isAuthenticated={isAuthenticated}
        isLoading={isLoading}
        onPrimaryAction={openStudio}
      />

      <CategoryRail
        categories={platformCategories}
        activeCategory={activeCategory}
        isPending={isCategoryPending}
        onSelect={selectCategory}
      />

      <section className="content-grid" aria-labelledby="catalog-title">
        <div className="catalog-column">
          <header className="section-title-row">
            <div>
              <p className="eyebrow">ARENA CATALOG</p>
              <h2 id="catalog-title">{selectedCategory.label} 토론</h2>
            </div>
            <p>{selectedCategory.description}</p>
          </header>

          <DebateGrid debates={visibleDebates} onStart={openStudio} />
        </div>

        <aside className="platform-sidebar" aria-label="추천 캐릭터와 최근 세션">
          <SpotlightCharacters onCreate={openStudio} />
          <RecentSessions sessions={recentPublicSessions} onStart={openStudio} />
        </aside>
      </section>

      <section className="studio-band" aria-labelledby="studio-title">
        <div>
          <p className="eyebrow">MAKE YOUR ARENA</p>
          <h2 id="studio-title">내 캐릭터로 바로 토론을 열어보세요.</h2>
          <p>
            공개 카탈로그는 지금은 하드코딩된 샘플입니다. 실제 생성은 로그인한
            사용자의 캐릭터와 기존 백엔드 API를 사용합니다.
          </p>
        </div>
        <button className="primary-button" type="button" onClick={openStudio}>
          {isAuthenticated ? "제작 스튜디오 열기" : "로그인하고 시작하기"}
        </button>
      </section>

      {studioOpen && data ? (
        <section className="studio-panel" aria-label="제작 스튜디오">
          <div className="studio-panel-header">
            <div>
              <p className="eyebrow">MY STUDIO</p>
              <h2>{data.user.nickname ?? "토론가"}님의 제작 공간</h2>
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
              user={data.user}
              characters={data.characters}
              onCreated={handleCharacterCreated}
              onLogout={handleLogout}
            />
            <DebateComposer characters={data.characters} />
          </div>
        </section>
      ) : null}

      {authOpen ? (
        <div className="auth-overlay" role="dialog" aria-modal="true">
          <button
            className="auth-backdrop"
            type="button"
            aria-label="인증 창 닫기"
            onClick={() => setAuthOpen(false)}
          />
          <AuthPanel
            onAuthenticated={handleAuthenticated}
            onCancel={() => setAuthOpen(false)}
          />
        </div>
      ) : null}
    </main>
  );
}

type TopNavigationProps = {
  isAuthenticated: boolean;
  user: User | undefined;
  onLogin: () => void;
  onOpenStudio: () => void;
};

function TopNavigation({
  isAuthenticated,
  user,
  onLogin,
  onOpenStudio,
}: TopNavigationProps) {
  return (
    <header className="top-navigation">
      <a className="brand-mark" href="#top" aria-label="AI Debate Arena 홈">
        <span>AI</span>
        Debate Arena
      </a>
      <nav aria-label="주요 메뉴">
        <a href="#catalog-title">토론 둘러보기</a>
        <a href="#studio-title">만들기</a>
        <a href="#characters">캐릭터</a>
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
          {isAuthenticated ? "내 스튜디오" : "로그인"}
        </button>
      </div>
    </header>
  );
}

type HeroSectionProps = {
  featuredDebate: PlatformDebateCard;
  isAuthenticated: boolean;
  isLoading: boolean;
  onPrimaryAction: () => void;
};

function HeroSection({
  featuredDebate,
  isAuthenticated,
  isLoading,
  onPrimaryAction,
}: HeroSectionProps) {
  return (
    <section className="hero-section" id="top">
      <div className="hero-copy">
        <p className="eyebrow">PUBLIC DEBATE CATALOG</p>
        <h1>AI 캐릭터들이 대신 끝까지 싸워주는 토론 놀이터.</h1>
        <p>
          인기 주제를 훑고, 마음에 드는 캐릭터를 고르고, 내 관점까지 섞어
          새로운 토론 세션을 만들 수 있습니다.
        </p>
        <div className="hero-actions">
          <button className="primary-button" type="button" onClick={onPrimaryAction}>
            {isAuthenticated ? "토론 만들기" : "무료로 둘러보고 시작"}
          </button>
          <span>{isLoading ? "세션 확인 중" : "로그인 없이도 탐색 가능"}</span>
        </div>
      </div>

      <article className="hero-feature" aria-label="대표 추천 토론">
        <span className="status-pill">{featuredDebate.status}</span>
        <div className="feature-orb" style={accentStyle(featuredDebate.accent)} />
        <p>{featuredDebate.categoryLabel}</p>
        <h2>{featuredDebate.title}</h2>
        <p>{featuredDebate.description}</p>
        <div className="participant-strip">
          {featuredDebate.participants.map((participant, index) => (
            <span key={participant}>
              {participant}
              <small>{featuredDebate.models[index]}</small>
            </span>
          ))}
        </div>
        <StatLine stats={featuredDebate.stats} />
      </article>
    </section>
  );
}

type CategoryRailProps = {
  categories: PlatformCategory[];
  activeCategory: string;
  isPending: boolean;
  onSelect: (categoryId: string) => void;
};

function CategoryRail({
  categories,
  activeCategory,
  isPending,
  onSelect,
}: CategoryRailProps) {
  return (
    <section className="category-rail" aria-label="토론 카테고리">
      {categories.map((category) => (
        <button
          key={category.id}
          className={category.id === activeCategory ? "active" : ""}
          type="button"
          aria-pressed={category.id === activeCategory}
          onClick={() => onSelect(category.id)}
        >
          {category.label}
        </button>
      ))}
      <span aria-live="polite">{isPending ? "정렬 중" : "카테고리 선택"}</span>
    </section>
  );
}

type DebateGridProps = {
  debates: PlatformDebateCard[];
  onStart: () => void;
};

function DebateGrid({ debates, onStart }: DebateGridProps) {
  return (
    <div className="debate-grid">
      {debates.map((debate) => (
        <article className="debate-card" key={debate.id}>
          <div className="debate-card-top">
            <span>{debate.categoryLabel}</span>
            <strong>{debate.status}</strong>
          </div>
          <div className="debate-card-mark" style={accentStyle(debate.accent)} />
          <h3>{debate.title}</h3>
          <p>{debate.description}</p>
          <div className="mini-participants">
            {debate.participants.map((participant) => (
              <span key={participant}>{participant}</span>
            ))}
          </div>
          <StatLine stats={debate.stats} />
          <button className="text-link-button" type="button" onClick={onStart}>
            이 주제로 시작하기
          </button>
        </article>
      ))}
    </div>
  );
}

function SpotlightCharacters({ onCreate }: { onCreate: () => void }) {
  return (
    <section className="sidebar-section" id="characters">
      <header>
        <p className="eyebrow">CHARACTER META</p>
        <h2>인기 캐릭터</h2>
      </header>
      <div className="spotlight-list">
        {characterSpotlights.map((character) => (
          <article key={character.id}>
            <div>
              <strong>{character.name}</strong>
              <span>{character.model}</span>
            </div>
            <p>{character.description}</p>
            <StatLine stats={character.stats} />
          </article>
        ))}
      </div>
      <button className="secondary-button wide" type="button" onClick={onCreate}>
        내 캐릭터 만들기
      </button>
    </section>
  );
}

type RecentSessionsProps = {
  sessions: PlatformDebateCard[];
  onStart: () => void;
};

function RecentSessions({ sessions, onStart }: RecentSessionsProps) {
  return (
    <section className="sidebar-section">
      <header>
        <p className="eyebrow">RECENT ROOMS</p>
        <h2>최근 열린 토론</h2>
      </header>
      <div className="recent-list">
        {sessions.map((session) => (
          <button key={session.id} type="button" onClick={onStart}>
            <span>{session.categoryLabel}</span>
            <strong>{session.title}</strong>
          </button>
        ))}
      </div>
    </section>
  );
}

function StatLine({ stats }: { stats: PlatformDebateCard["stats"] }) {
  return (
    <dl className="stat-line">
      {stats.map((stat) => (
        <div key={`${stat.label}-${stat.value}`}>
          <dt>{stat.label}</dt>
          <dd>{stat.value}</dd>
        </div>
      ))}
    </dl>
  );
}

function accentStyle(accent: string): CSSProperties {
  return { "--accent": accent } as CSSProperties;
}
