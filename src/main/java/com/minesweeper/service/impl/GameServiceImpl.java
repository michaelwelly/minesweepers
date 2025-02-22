package com.minesweeper.service.impl;

import com.minesweeper.dto.TurnRequest;
import com.minesweeper.model.Cell;
import com.minesweeper.model.Game;
import com.minesweeper.model.GameState;
import com.minesweeper.repository.CellRepository;
import com.minesweeper.repository.GameRepository;
import com.minesweeper.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Random;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    private final GameRepository gameRepository;
    private final CellRepository cellRepository;

    public GameServiceImpl(GameRepository gameRepository, CellRepository cellRepository) {
        this.gameRepository = gameRepository;
        this.cellRepository = cellRepository;
    }

    @Override
    @Transactional
    public Game createNewGame(int width, int height, int minesCount) {
        Game game = new Game(width, height, minesCount);
        game = gameRepository.save(game);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                cellRepository.save(new Cell(game, row, col));
            }
        }

        placeMines(game);
        return gameRepository.save(game);
    }

    @Override
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    @Override
    @Transactional
    public Game makeMove(Long gameId, int row, int col) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Игра не найдена или уже завершена."));

        Cell cell = cellRepository.findByGameId(gameId)
                .stream()
                .filter(c -> c.getRow() == row && c.getCol() == col)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ячейка не найдена."));

        if (cell.isRevealed()) {
            throw new IllegalStateException("Эта ячейка уже открыта.");
        }

        cell.setRevealed(true);
        cellRepository.save(cell);

        if (cell.isHasMine()) {
            game.setState(GameState.LOST);
        } else if (cell.getSurroundingMines() == 0) {
            revealAdjacentCells(game, row, col);
        }

        if (checkWin(game)) {
            game.setState(GameState.WON);
        }

        return gameRepository.save(game);
    }

    @PostMapping("/turn")
    public ResponseEntity<Game> makeMove(@RequestBody TurnRequest request) {
        log.info("Received request: gameId={}, row={}, col={}", request.getGameId(), request.getRow(), request.getCol());
        try {
            Long gameId = Long.parseLong(request.getGameId()); // Преобразуем String в Long
            Game game = makeMove(gameId, request.getRow(), request.getCol());
            return ResponseEntity.ok(game);
        } catch (NumberFormatException e) {
            log.error("Invalid gameId format: {}", request.getGameId());
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            log.error("Error processing move: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    private void placeMines(Game game) {
        List<Cell> cells = cellRepository.findByGameId(game.getId());
        Random rand = new Random();
        int placedMines = 0;

        while (placedMines < game.getMinesCount()) {
            Cell cell = cells.get(rand.nextInt(cells.size()));
            if (!cell.isHasMine()) {
                cell.setHasMine(true);
                placedMines++;
                cellRepository.save(cell);
            }
        }
    }

    private boolean checkWin(Game game) {
        return cellRepository.findByGameId(game.getId()).stream()
                .allMatch(cell -> cell.isHasMine() || cell.isRevealed());
    }

    private void revealAdjacentCells(Game game, int row, int col) {
        List<Cell> cells = cellRepository.findByGameId(game.getId());

        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = row + dx[i];
            int newCol = col + dy[i];

            cells.stream()
                    .filter(c -> c.getRow() == newRow && c.getCol() == newCol && !c.isRevealed() && !c.isHasMine())
                    .findFirst()
                    .ifPresent(cell -> {
                        cell.setRevealed(true);
                        cellRepository.save(cell);
                        if (cell.getSurroundingMines() == 0) {
                            revealAdjacentCells(game, newRow, newCol);
                        }
                    });
        }
    }

    @Override
    public Game findGameById(Long id) {
        return gameRepository.findById(id).orElse(null);
    }
}