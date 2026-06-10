package com.example.aichat.character.application;

import java.util.List;

public record ListCharactersResult(List<CharacterView> items) {

    public ListCharactersResult {
        items = List.copyOf(items);
    }
}
