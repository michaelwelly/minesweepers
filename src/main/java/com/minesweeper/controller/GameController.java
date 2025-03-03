package com.minesweeper.controller;

import com.minesweeper.dto.TurnRequest;
import com.minesweeper.model.Game;
import com.minesweeper.model.GameDifficulty;
import com.minesweeper.model.User;
import com.minesweeper.service.GameService;
import com.minesweeper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;
    private final UserService userService;


    @PostMapping("/new/standard")
    public ResponseEntity<Game> startStandardGame(@RequestParam UUID userId, @RequestParam String difficulty) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            GameDifficulty gameDifficulty = GameDifficulty.valueOf(difficulty.toUpperCase());
            Game game = gameService.createStandardGame(user, gameDifficulty);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            log.error("Invalid difficulty level: {}", difficulty);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/new/custom")
    public ResponseEntity<Game> startCustomGame(@RequestParam UUID userId, @RequestParam int width,
                                                @RequestParam int height, @RequestParam int mines) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Game game = gameService.createCustomGame(user, width, height, mines);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for custom game");
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable UUID gameId) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping("/turn")
    public ResponseEntity<Game> makeMove(@RequestBody TurnRequest request) {
        log.info("Received request: userId={}, gameId={}, row={}, col={}",
                request.getUserId(), request.getGameId(), request.getRow(), request.getCol());

        if (request.getGameId() == null || request.getGameId().isEmpty() ||
                request.getUserId() == null || request.getUserId().isEmpty()) {
            log.error("Received request with null or empty gameId or userId");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            UUID gameId = UUID.fromString(request.getGameId().trim());
            UUID userId = UUID.fromString(request.getUserId().trim());

            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Game game = gameService.makeMove(user, gameId, request.getRow(), request.getCol());
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for gameId or userId: gameId={}, userId={}",
                    request.getGameId(), request.getUserId(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            log.error("Error processing move: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}