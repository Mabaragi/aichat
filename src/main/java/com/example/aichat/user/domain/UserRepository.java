package com.example.aichat.user.domain;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long userId);

    default Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    default boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
