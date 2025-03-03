package com.minesweeper.service;

import com.minesweeper.model.*;
import com.minesweeper.repository.CellRepository;
import com.minesweeper.repository.GameRepository;
import com.minesweeper.repository.UserRepository;
import com.minesweeper.service.impl.GameServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {
    private static final Logger log = LoggerFactory.getLogger(GameServiceTest.class);

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CellRepository cellRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GameServiceImpl gameService;

    private AutoCloseable mocks;
    private User testUser;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        log.info("Initializing test setup...");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("test_player");

        when(userService.getUserById(testUser.getId())).thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        log.info("Test execution completed.");
    }

    @Test
    void testCreateStandardGame() {
        GameDifficulty difficulty = GameDifficulty.EASY;
        UUID gameId = UUID.randomUUID();
        Game game = new Game(testUser, difficulty.getWidth(), difficulty.getHeight(), difficulty.getMines());
        game.setId(gameId);

        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game createdGame = gameService.createStandardGame(testUser, difficulty);

        assertNotNull(createdGame);
        assertEquals(difficulty.getWidth(), createdGame.getWidth());
        assertEquals(difficulty.getHeight(), createdGame.getHeight());
        assertEquals(difficulty.getMines(), createdGame.getMinesCount());
    }

    @Test
    void testCreateCustomGame() {
        int width = 10, height = 10, mines = 20;
        UUID gameId = UUID.randomUUID();
        Game game = new Game(testUser, width, height, mines);
        game.setId(gameId);

        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game createdGame = gameService.createCustomGame(testUser, width, height, mines);

        assertNotNull(createdGame);
        assertEquals(width, createdGame.getWidth());
        assertEquals(height, createdGame.getHeight());
        assertEquals(mines, createdGame.getMinesCount());
    }

    @Test
    void testMakeMove_OpenSafeCell() {
        UUID gameId = UUID.randomUUID();
        int row = 1, col = 1;
        Game game = new Game(testUser, 5, 5, 5);
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        Cell cell = new Cell(game, row, col);
        cell.setHasMine(false);
        cell.setRevealed(false);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(List.of(cell));
        when(cellRepository.save(any(Cell.class))).thenReturn(cell);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game updatedGame = gameService.makeMove(testUser, gameId, row, col);

        assertNotNull(updatedGame);
        assertTrue(cell.isRevealed());
        assertEquals(GameState.IN_PROGRESS, updatedGame.getState());
    }

    @Test
    void testMakeMove_HitMine() {
        UUID gameId = UUID.randomUUID();
        int row = 2, col = 3;
        Game game = new Game(testUser, 5, 5, 5);
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        Cell cell = new Cell(game, row, col);
        cell.setHasMine(true);
        cell.setRevealed(false);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(List.of(cell));

        Game updatedGame = gameService.makeMove(testUser, gameId, row, col);

        assertEquals(GameState.LOST, updatedGame.getState());
    }

    @Test
    void testMakeMove_AlreadyRevealedCell() {
        UUID gameId = UUID.randomUUID();
        int row = 2, col = 3;
        Game game = new Game(testUser, 5, 5, 5);
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        Cell cell = new Cell(game, row, col);
        cell.setHasMine(false);
        cell.setRevealed(true);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Game updatedGame = gameService.makeMove(testUser, gameId, row, col);

        assertEquals(GameState.IN_PROGRESS, updatedGame.getState());
    }

    @Test
    void testCheckWin_AllSafeCellsRevealed() {
        UUID gameId = UUID.randomUUID();
        Game game = new Game(testUser, 3, 3, 1);
        game.setId(gameId);

        Cell mineCell = new Cell(game, 0, 0);
        mineCell.setHasMine(true);
        mineCell.setRevealed(false);

        Cell safeCell = new Cell(game, 0, 1);
        safeCell.setHasMine(false);
        safeCell.setRevealed(true);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Game updatedGame = gameService.makeMove(testUser, gameId, 0, 1);

        assertEquals(GameState.WON, updatedGame.getState());
    }

    @Test
    void testFullGamePlaythrough() {
        UUID gameId = UUID.randomUUID();
        Game game = new Game(testUser, 3, 3, 1);
        game.setId(gameId);
        game.setState(GameState.IN_PROGRESS);

        Cell safeCell1 = new Cell(game, 0, 1);
        safeCell1.setHasMine(false);
        safeCell1.setRevealed(false);

        Cell safeCell2 = new Cell(game, 1, 1);
        safeCell2.setHasMine(false);
        safeCell2.setRevealed(false);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(cellRepository.findByGameId(gameId)).thenReturn(List.of(safeCell1, safeCell2));

        gameService.makeMove(testUser, gameId, 0, 1);
        assertTrue(safeCell1.isRevealed());

        gameService.makeMove(testUser, gameId, 1, 1);
        assertTrue(safeCell2.isRevealed());
        assertEquals(GameState.WON, game.getState());
    }
}