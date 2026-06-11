ALTER TABLE characters ADD COLUMN persona TEXT;
ALTER TABLE debate_participants ADD COLUMN persona TEXT;

UPDATE characters
SET persona = json_object(
    'identity', coalesce(nullif(description, ''), '토론 캐릭터'),
    'debateRole', '토론 참가자',
    'coreValues', json_array('사실성', '논리적 일관성'),
    'expertise', json_array(),
    'defaultStance', '논제에 따라 입장을 형성한다.',
    'evidenceStyle', '근거와 논리를 우선한다.',
    'debateBehavior', json_array(),
    'voiceStyle', json_object(
        'tone', CASE
            WHEN speech_style IS NOT NULL AND json_valid(speech_style)
                THEN CAST(coalesce(json_extract(speech_style, '$.tone'), '차분함') AS TEXT)
            ELSE '차분함'
        END,
        'sentenceLength', CASE
            WHEN speech_style IS NOT NULL AND json_valid(speech_style)
                THEN CAST(coalesce(json_extract(speech_style, '$.sentenceLength'), '중간') AS TEXT)
            ELSE '중간'
        END,
        'rhetoricalStyle', CASE
            WHEN speech_style IS NOT NULL AND json_valid(speech_style)
                THEN CAST(coalesce(json_extract(speech_style, '$.rhetoricalStyle'), '논리적 반박 중심') AS TEXT)
            ELSE '논리적 반박 중심'
        END,
        'signaturePhrases', json_array()
    ),
    'boundaries', json_object(
        'mustDo', json_array('상대 주장을 먼저 요약한다.', '불확실한 사실은 단정하지 않는다.'),
        'mustNotDo', json_array('인신공격하지 않는다.', '허위 사실을 단정하지 않는다.', '상대 주장을 왜곡하지 않는다.')
    ),
    'exampleLines', json_array()
)
WHERE persona IS NULL;

UPDATE debate_participants
SET persona = json_object(
    'identity', coalesce(nullif(description, ''), '토론 캐릭터'),
    'debateRole', '토론 참가자',
    'coreValues', json_array('사실성', '논리적 일관성'),
    'expertise', json_array(),
    'defaultStance', '논제에 따라 입장을 형성한다.',
    'evidenceStyle', '근거와 논리를 우선한다.',
    'debateBehavior', json_array(),
    'voiceStyle', json_object(
        'tone', CASE
            WHEN speech_style IS NOT NULL AND json_valid(speech_style)
                THEN CAST(coalesce(json_extract(speech_style, '$.tone'), '차분함') AS TEXT)
            ELSE '차분함'
        END,
        'sentenceLength', CASE
            WHEN speech_style IS NOT NULL AND json_valid(speech_style)
                THEN CAST(coalesce(json_extract(speech_style, '$.sentenceLength'), '중간') AS TEXT)
            ELSE '중간'
        END,
        'rhetoricalStyle', CASE
            WHEN speech_style IS NOT NULL AND json_valid(speech_style)
                THEN CAST(coalesce(json_extract(speech_style, '$.rhetoricalStyle'), '논리적 반박 중심') AS TEXT)
            ELSE '논리적 반박 중심'
        END,
        'signaturePhrases', json_array()
    ),
    'boundaries', json_object(
        'mustDo', json_array('상대 주장을 먼저 요약한다.', '불확실한 사실은 단정하지 않는다.'),
        'mustNotDo', json_array('인신공격하지 않는다.', '허위 사실을 단정하지 않는다.', '상대 주장을 왜곡하지 않는다.')
    ),
    'exampleLines', json_array()
)
WHERE persona IS NULL;
