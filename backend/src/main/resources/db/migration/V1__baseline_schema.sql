CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL DEFAULT '',
    nickname VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT NOT NULL,
    family_id VARCHAR(36) NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    replaced_by_hash VARCHAR(64),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS characters (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    personality TEXT,
    speech_style TEXT,
    visibility VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS debate_sessions (
    id BIGINT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    topic_title VARCHAR(255) NOT NULL,
    topic_description TEXT,
    topic_category VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    format VARCHAR(255) NOT NULL,
    max_rounds INTEGER NOT NULL,
    current_round INTEGER NOT NULL,
    max_turn_length INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    ended_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS debate_participants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id BIGINT NOT NULL,
    source_character_id BIGINT NOT NULL,
    position INTEGER NOT NULL,
    model VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    personality TEXT,
    speech_style TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_debate_participant_session_position
    ON debate_participants(session_id, position);
CREATE INDEX IF NOT EXISTS idx_debate_participant_session_position
    ON debate_participants(session_id, position);

CREATE TABLE IF NOT EXISTS debate_turns (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    round INTEGER NOT NULL,
    turn_index INTEGER NOT NULL,
    type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    content TEXT,
    prompt_snapshot TEXT,
    model_name VARCHAR(255),
    input_tokens INTEGER,
    output_tokens INTEGER,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_debate_turn_session_index
    ON debate_turns(session_id, turn_index);
CREATE INDEX IF NOT EXISTS idx_debate_turn_session_index
    ON debate_turns(session_id, turn_index);
