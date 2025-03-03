package com.minesweeper.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum GameDifficulty {
    EASY(9, 9, 10),
    MEDIUM(16, 16, 40),
    OLYMPIC(30, 16, 99);

    private final int width;
    private final int height;
    private final int mines;

    public static GameDifficulty fromString(String difficulty) {
        return Arrays.stream(GameDifficulty.values())
                .filter(d -> d.name().equalsIgnoreCase(difficulty))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid difficulty level: " + difficulty));
    }
}