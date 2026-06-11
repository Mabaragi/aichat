CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scope VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_categories_scope_slug UNIQUE (scope, slug)
);

CREATE INDEX IF NOT EXISTS idx_categories_scope_order
    ON categories(scope, active, display_order, id);

INSERT OR IGNORE INTO categories(scope, slug, name, description, display_order, active, created_at, updated_at)
VALUES
    ('DEBATE', 'food', '음식', '음식과 취향을 다루는 토론', 10, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('DEBATE', 'culture', '문화', '콘텐츠와 문화 현상을 다루는 토론', 20, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('DEBATE', 'tech', '기술', '기술 선택과 제품 방향을 다루는 토론', 30, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('DEBATE', 'life', '생활', '일상 선택과 습관을 다루는 토론', 40, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('DEBATE', 'society', '사회', '사회 이슈와 제도를 다루는 토론', 50, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('DEBATE', 'fun', '재미', '가볍고 빠르게 즐기는 토론', 60, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('DEBATE', 'other', '기타', '기타 토론', 999, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('CHARACTER', 'expert', '전문가', '근거와 전문성을 강조하는 캐릭터', 10, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('CHARACTER', 'critic', '비평가', '논점을 날카롭게 검토하는 캐릭터', 20, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('CHARACTER', 'creator', '창작자', '새로운 관점과 아이디어를 제안하는 캐릭터', 30, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('CHARACTER', 'storyteller', '스토리텔러', '사례와 서사로 설득하는 캐릭터', 40, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('CHARACTER', 'comedy', '코미디', '유머와 반전을 사용하는 캐릭터', 50, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('CHARACTER', 'utility', '실용', '요약과 정리를 돕는 캐릭터', 60, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00'),
    ('CHARACTER', 'other', '기타', '기타 캐릭터', 999, 1, '2026-06-11T00:00:00', '2026-06-11T00:00:00');

ALTER TABLE characters ADD COLUMN category_id BIGINT;
UPDATE characters
SET category_id = (SELECT id FROM categories WHERE scope = 'CHARACTER' AND slug = 'other')
WHERE category_id IS NULL;

ALTER TABLE debate_sessions ADD COLUMN category_id BIGINT;
ALTER TABLE debate_sessions ADD COLUMN visibility VARCHAR(255) NOT NULL DEFAULT 'PRIVATE';
UPDATE debate_sessions
SET topic_category = lower(coalesce(nullif(topic_category, ''), 'other'))
WHERE topic_category IS NULL OR topic_category = '' OR topic_category != lower(topic_category);
UPDATE debate_sessions
SET category_id = (
    SELECT id FROM categories
    WHERE scope = 'DEBATE' AND slug = debate_sessions.topic_category
)
WHERE category_id IS NULL;
UPDATE debate_sessions
SET category_id = (SELECT id FROM categories WHERE scope = 'DEBATE' AND slug = 'other'),
    topic_category = 'other'
WHERE category_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_characters_public_category
    ON characters(visibility, category_id, updated_at, id);
CREATE INDEX IF NOT EXISTS idx_debate_sessions_public_category
    ON debate_sessions(visibility, status, category_id, ended_at, id);
