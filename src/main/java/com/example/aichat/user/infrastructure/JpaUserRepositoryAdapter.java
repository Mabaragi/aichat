package com.example.aichat.user.infrastructure;

import com.example.aichat.user.domain.User;
import com.example.aichat.user.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public JpaUserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity persisted = userJpaRepository.save(toEntity(user));
        return toDomain(persisted);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId)
                .map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(User.normalizeEmail(email))
                .map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(User.normalizeEmail(email));
    }

    private static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }

    private static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.id(),
                entity.email(),
                entity.passwordHash(),
                entity.nickname(),
                entity.createdAt()
        );
    }
}
