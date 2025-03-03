package com.minesweeper.controller;

import com.minesweeper.model.GameDifficulty;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameDifficultyTest {

    @Test
    void testValidDifficulty() {
        assertEquals(GameDifficulty.EASY, GameDifficulty.fromString("easy"));
        assertEquals(GameDifficulty.MEDIUM, GameDifficulty.fromString("medium"));
        assertEquals(GameDifficulty.OLYMPIC, GameDifficulty.fromString("olympic"));
    }

    @Test
    void testInvalidDifficulty() {
        assertThrows(IllegalArgumentException.class, () -> GameDifficulty.fromString("invalid"));
        assertThrows(IllegalArgumentException.class, () -> GameDifficulty.fromString("123"));
    }
}