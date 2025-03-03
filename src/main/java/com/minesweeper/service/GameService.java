package com.minesweeper.service;

import com.minesweeper.model.Game;
import com.minesweeper.model.GameDifficulty;
import com.minesweeper.model.User;
import jakarta.transaction.Transactional;

import java.util.UUID;

public interface GameService {

    /**
     * Creates a new game with a predefined difficulty level for a specific user.
     * @param user the user who starts the game
     * @param difficulty the difficulty level (EASY, MEDIUM, OLYMPIC)
     * @return the created game
     */
    Game createStandardGame(User user, GameDifficulty difficulty);

    /**
     * Creates a new custom game with the given parameters for a specific user.
     * @param user the user who starts the game
     * @param width the width of the game board
     * @param height the height of the game board
     * @param minesCount the number of mines
     * @return the created game
     */
    Game createCustomGame(User user, int width, int height, int minesCount);

    /**
     * Retrieves a game by its identifier.
     * @param gameId the game identifier
     * @return the Game object
     */
    Game getGame(UUID gameId);

    /**
     * Processes the player's move.
     * @param user the user making the move
     * @param gameId the game identifier
     * @param row the row of the move
     * @param col the column of the move
     * @return the updated Game object
     */
    @Transactional
    Game makeMove(User user, UUID gameId, int row, int col);
    /**
     * Finds a game by its identifier.
     * @param id the game identifier
     * @return the Game object or null if the game is not found
     */
    Game findGameById(UUID id);
}