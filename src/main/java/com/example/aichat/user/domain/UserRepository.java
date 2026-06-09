package com.example.aichat.user.domain;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long userId);
}
