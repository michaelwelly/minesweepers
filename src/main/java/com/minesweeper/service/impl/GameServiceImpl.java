package com.minesweeper.service.impl;

import com.minesweeper.dto.TurnRequest;
import com.minesweeper.model.*;
import com.minesweeper.repository.CellRepository;
import com.minesweeper.repository.GameRepository;
import com.minesweeper.repository.UserRepository;
import com.minesweeper.service.GameService;
import com.minesweeper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    private final UserService userService;

    private final GameRepository gameRepository;
    private final CellRepository cellRepository;
    private final UserRepository userRepository;
    
    public GameServiceImpl(UserService userService, GameRepository gameRepository, CellRepository cellRepository, UserRepository userRepository) {
        this.userService = userService;
        this.gameRepository = gameRepository;
        this.cellRepository = cellRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Game createStandardGame(User user, GameDifficulty difficulty) {
        Game game = new Game(user, difficulty.getWidth(), difficulty.getHeight(), difficulty.getMines());
        game = gameRepository.save(game);
        generateCells(game);
        placeMines(game);
        return gameRepository.save(game);
    }


    @Override
    @Transactional
    public Game createCustomGame(User user, int width, int height, int minesCount) {
        Game game = new Game(user, width, height, minesCount);
        game = gameRepository.save(game);
        generateCells(game);
        placeMines(game);
        return gameRepository.save(game);
    }

    @Override
    public Game getGame(UUID gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    @Override
    public Game findGameById(UUID id) {
        return gameRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Game makeMove(User user, UUID gameId, int row, int col) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found or already finished."));
        log.info("LOG: Found game with ID: {}, status: {}", game.getId(), game.getState());

        if (game.getUser() == null || !Objects.equals(game.getUser().getId(), user.getId())) {
            throw new IllegalStateException("User is not allowed to play this game.");
        }

        if (game.getState() != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is already finished.");
        }

        List<Cell> cells = cellRepository.findByGameId(gameId);
        if (cells.isEmpty()) {
            throw new IllegalStateException("No cells found for this game.");
        }

        Cell cell = cells.stream()
                .filter(c -> c.getRow() == row && c.getCol() == col)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cell not found."));

        if (cell.isRevealed()) {
            log.info("LOG: Cell ({}, {}) is already revealed. Ignoring move.", row, col);
            return game;
        }

        cell.setRevealed(true);
        cellRepository.save(cell);

        if (cell.isHasMine()) {
            game.setState(GameState.LOST);
            gameRepository.save(game);
            log.info("LOG: Player hit a mine at ({}, {}). Game over!", row, col);
            return game;
        }

        if (cell.getSurroundingMines() == 0) {
            revealAdjacentCells(game, row, col);
        }

        if (checkWin(game)) {
            game.setState(GameState.WON);
            gameRepository.save(game);
            log.info("LOG: Game {} won! All safe cells revealed.", game.getId());
        }

        log.info("LOG: Saved game with ID: {}, status: {}", game.getId(), game.getState());
        return game;
    }

    private void placeMines(Game game) {
        List<Cell> cells = cellRepository.findByGameId(game.getId());
        log.info("LOG: Found {} cells for game ID: {}", cells.size(), game.getId());

        if (cells.isEmpty()) {
            throw new IllegalStateException("Cannot place mines: no cells available in the game.");
        }

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
        if (game.getState() == GameState.LOST) {
            return false;
        }

        long revealedSafeCells = cellRepository.findByGameId(game.getId()).stream()
                .filter(cell -> !cell.isHasMine() && cell.isRevealed())
                .count();
        long totalSafeCells = cellRepository.findByGameId(game.getId()).stream()
                .filter(cell -> !cell.isHasMine())
                .count();

        log.info("LOG: Checking win condition for game {} - Revealed: {}, Total: {}",
                game.getId(), revealedSafeCells, totalSafeCells);

        return revealedSafeCells == totalSafeCells;
    }

    private void generateCells(Game game) {
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Cell cell = new Cell(game, row, col);
                cell.setHasMine(false);
                cell.setRevealed(false);
                cell.setSurroundingMines(0);
                cellRepository.save(cell);
            }
        }
        log.info("LOG: Generated {} cells for game ID: {}", game.getWidth() * game.getHeight(), game.getId());
    }

    private void revealAdjacentCells(Game game, int row, int col) {
        List<Cell> cells = cellRepository.findByGameId(game.getId());

        // Создаем Map для быстрого доступа к клеткам по координатам
        Map<String, Cell> cellMap = cells.stream()
                .collect(Collectors.toMap(cell -> cell.getRow() + "," + cell.getCol(), cell -> cell));

        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        Queue<Cell> queue = new LinkedList<>();
        queue.add(cellMap.get(row + "," + col));

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current == null || current.isRevealed() || current.isHasMine()) {
                continue;
            }

            current.setRevealed(true);

            if (current.getSurroundingMines() == 0) {
                for (int i = 0; i < 8; i++) {
                    int newRow = current.getRow() + dx[i];
                    int newCol = current.getCol() + dy[i];
                    String key = newRow + "," + newCol;

                    if (cellMap.containsKey(key) && !cellMap.get(key).isRevealed()) {
                        queue.add(cellMap.get(key));
                    }
                }
            }
        }

        cellRepository.saveAll(cellMap.values());
    }
}