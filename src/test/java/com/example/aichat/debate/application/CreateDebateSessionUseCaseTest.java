package com.example.aichat.debate.application;

import com.example.aichat.character.domain.Character;
import com.example.aichat.character.domain.CharacterRepository;
import com.example.aichat.character.domain.Personality;
import com.example.aichat.character.domain.SpeechStyle;
import com.example.aichat.common.exception.BusinessException;
import com.example.aichat.common.exception.ErrorCode;
import com.example.aichat.debate.domain.DebateFormat;
import com.example.aichat.debate.domain.DebateSession;
import com.example.aichat.debate.domain.DebateSessionRepository;
import com.example.aichat.debate.domain.ParticipantModel;
import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateDebateSessionUseCaseTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 10, 12, 0);

    private InMemoryUserRepository userRepository;
    private InMemoryCharacterRepository characterRepository;
    private InMemoryDebateSessionRepository debateSessionRepository;
    private CreateDebateSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        characterRepository = new InMemoryCharacterRepository();
        debateSessionRepository = new InMemoryDebateSessionRepository();
        useCase = new CreateDebateSessionUseCase(
                userRepository,
                characterRepository,
                debateSessionRepository,
                () -> FIXED_TIME
        );
        userRepository.add(new User(
                1L, "owner@example.com", "owner", FIXED_TIME));
    }

    @Test
    void createSessionWithOwnedPrivateAndOtherUsersPublicCharacters() {
        characterRepository.add(character(10L, 1L, "내 캐릭터", "PRIVATE"));
        characterRepository.add(character(20L, 2L, "공개 캐릭터", "PUBLIC"));

        DebateSessionView created = useCase.execute(command(
                new CreateDebateParticipantCommand(10L, ParticipantModel.FAST),
                new CreateDebateParticipantCommand(20L, ParticipantModel.QUALITY)
        ));

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.ownerId()).isEqualTo(1L);
        assertThat(created.participants())
                .extracting(DebateParticipantView::position)
                .containsExactly(0, 1);
        assertThat(created.participants())
                .extracting(DebateParticipantView::name)
                .containsExactly("내 캐릭터", "공개 캐릭터");
    }

    @Test
    void allowSameCharacterTwiceAndKeepIndependentModels() {
        characterRepository.add(character(10L, 1L, "내 캐릭터", "PRIVATE"));

        DebateSessionView created = useCase.execute(command(
                new CreateDebateParticipantCommand(10L, ParticipantModel.FAST),
                new CreateDebateParticipantCommand(10L, ParticipantModel.QUALITY)
        ));

        assertThat(created.participants())
                .extracting(DebateParticipantView::sourceCharacterId)
                .containsExactly(10L, 10L);
        assertThat(created.participants())
                .extracting(DebateParticipantView::model)
                .containsExactly(ParticipantModel.FAST, ParticipantModel.QUALITY);
    }

    @Test
    void rejectMissingUser() {
        userRepository.users.clear();

        assertThatThrownBy(() -> useCase.execute(command(
                new CreateDebateParticipantCommand(10L, ParticipantModel.FAST),
                new CreateDebateParticipantCommand(20L, ParticipantModel.QUALITY)
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void rejectMissingOrOtherUsersPrivateCharacter() {
        characterRepository.add(character(20L, 2L, "비공개 캐릭터", "PRIVATE"));

        assertThatThrownBy(() -> useCase.execute(command(
                new CreateDebateParticipantCommand(20L, ParticipantModel.FAST),
                new CreateDebateParticipantCommand(99L, ParticipantModel.QUALITY)
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.CHARACTER_NOT_FOUND));
    }

    @Test
    void savedParticipantKeepsSnapshotAfterCharacterChanges() {
        Character source = character(10L, 1L, "원본 이름", "PRIVATE");
        characterRepository.add(source);

        DebateSessionView created = useCase.execute(command(
                new CreateDebateParticipantCommand(10L, ParticipantModel.FAST),
                new CreateDebateParticipantCommand(10L, ParticipantModel.QUALITY)
        ));

        source.update(
                "수정 이름",
                "수정 설명",
                Personality.of("{\"rationality\":10}"),
                SpeechStyle.of("{\"tone\":\"반말\"}"),
                "PUBLIC",
                FIXED_TIME.plusHours(1)
        );

        DebateSession saved = debateSessionRepository.findById(created.id()).orElseThrow();
        assertThat(saved.getParticipants())
                .extracting(participant -> participant.getName())
                .containsOnly("원본 이름");
        assertThat(saved.getParticipants())
                .extracting(participant -> participant.getPersonality())
                .containsOnly("{\"rationality\":90}");
    }

    private static CreateDebateSessionCommand command(CreateDebateParticipantCommand first,
                                                       CreateDebateParticipantCommand second) {
        return new CreateDebateSessionCommand(
                1L,
                "부먹 vs 찍먹",
                "어느 방식이 더 나은가?",
                "FOOD",
                DebateFormat.PROS_AND_CONS,
                5,
                600,
                List.of(first, second)
        );
    }

    private static Character character(Long id, Long ownerId, String name, String visibility) {
        return new Character(
                id,
                ownerId,
                name,
                "논리적인 캐릭터",
                Personality.of("{\"rationality\":90}"),
                SpeechStyle.of("{\"tone\":\"차분함\"}"),
                visibility,
                FIXED_TIME,
                FIXED_TIME
        );
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<Long, User> users = new HashMap<>();

        void add(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public User save(User user) {
            users.put(user.getId(), user);
            return user;
        }

        @Override
        public Optional<User> findById(Long userId) {
            return Optional.ofNullable(users.get(userId));
        }
    }

    private static final class InMemoryCharacterRepository implements CharacterRepository {
        private final Map<Long, Character> characters = new HashMap<>();

        void add(Character character) {
            characters.put(character.getId(), character);
        }

        @Override
        public Character save(Character character) {
            characters.put(character.getId(), character);
            return character;
        }

        @Override
        public Optional<Character> findById(Long characterId) {
            return Optional.ofNullable(characters.get(characterId));
        }

        @Override
        public List<Character> findByOwnerId(Long ownerId) {
            return characters.values().stream()
                    .filter(character -> character.getOwnerId().equals(ownerId))
                    .toList();
        }

        @Override
        public void deleteById(Long characterId) {
            characters.remove(characterId);
        }
    }

    private static final class InMemoryDebateSessionRepository implements DebateSessionRepository {
        private final AtomicLong nextId = new AtomicLong(1L);
        private final Map<Long, DebateSession> sessions = new HashMap<>();

        @Override
        public DebateSession save(DebateSession session) {
            Long id = session.getId() == null ? nextId.getAndIncrement() : session.getId();
            DebateSession saved = new DebateSession(
                    id,
                    session.getOwnerId(),
                    session.getTopicTitle(),
                    session.getTopicDescription(),
                    session.getTopicCategory(),
                    session.getStatus(),
                    session.getFormat(),
                    session.getMaxRounds(),
                    session.getCurrentRound(),
                    session.getMaxTurnLength(),
                    session.getParticipants(),
                    session.getCreatedAt(),
                    session.getStartedAt(),
                    session.getEndedAt()
            );
            sessions.put(id, saved);
            return saved;
        }

        @Override
        public Optional<DebateSession> findById(Long sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }
    }
}
