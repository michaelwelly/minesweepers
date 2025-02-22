package com.minesweeper.controller;

import com.minesweeper.dto.TurnRequest;
import com.minesweeper.model.Game;
import com.minesweeper.service.GameService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    // 1️⃣ Создание новой игры
    @PostMapping("/new")
    public ResponseEntity<Game> startNewGame(@RequestBody Map<String, Integer> request) {
        int width = request.getOrDefault("width", 10);
        int height = request.getOrDefault("height", 10);
        int mines = request.getOrDefault("mines_count", 10);

        Game game = gameService.createNewGame(width, height, mines);
        return ResponseEntity.ok(game);
    }

    // 2️⃣ Получение информации об игре
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable Long gameId) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    // 3️⃣ Обработка хода игрока
    @PostMapping("/turn")
    public ResponseEntity<Game> makeMove(@RequestBody TurnRequest request) {
        log.info("Received request: gameId={}, row={}, col={}", request.getGameId(), request.getRow(), request.getCol());
        try {
            Long gameId = Long.parseLong(request.getGameId()); // Преобразуем String в Long
            Game game = gameService.makeMove(gameId, request.getRow(), request.getCol());
            return ResponseEntity.ok(game);
        } catch (NumberFormatException e) {
            log.error("Invalid gameId format: {}", request.getGameId());
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            log.error("Error processing move: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}